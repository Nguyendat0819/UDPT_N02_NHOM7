        var stompClient = null;
        var myUUID = document.getElementById("currentUserId").value; 
        var currentRecipientEmail = null; 

        // 1. XỬ LÝ AVATAR
        function getAvatarHtml(user) {
            if (user.avatarUrl && user.avatarUrl.trim() !== "") {
                var src = user.avatarUrl;
                if (!src.startsWith("/") && !src.startsWith("http")) src = "/uploads/" + src; 
                return `<img src="${src}" class="avatar-img" onerror="this.parentNode.innerHTML='${user.username.charAt(0).toUpperCase()}'">`;
            }
            return user.username.charAt(0).toUpperCase();
        }

        // 2. KẾT NỐI WEBSOCKET
        function connect() {
            var socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null; 

            stompClient.connect({}, function (frame) {
                console.log('✅ Đã kết nối WebSocket!');
                
                // Đăng ký nhận tin
                stompClient.subscribe('/user/queue/messages', function (messageOutput) {
                    var message = JSON.parse(messageOutput.body);
                    console.log("📩 Nhận được tin nhắn từ Server:", message);

                    // LOGIC ĐƠN GIẢN HÓA:
                    // 1. Nếu là tin của mình gửi -> Bỏ qua (vì đã hiện rồi)
                    if (String(message.senderId) === String(myUUID)) {
                        return;
                    }

                    // 2. Nếu là tin người khác -> HIỆN LUÔN (Không cần check ID gì cả để test)
                    displayMessage(message);
                    
                    // (Tùy chọn) Bật Alert lên để biết chắc chắn có tin đến
                    // alert("Có tin nhắn mới từ: " + message.senderId);
                });
            });
        }

        // 3. LẤY DANH SÁCH USER
        function loadUsers() {
            fetch('/api/users').then(res => res.json()).then(users => {
                var list = document.getElementById("userList");
                list.innerHTML = "";
                users.forEach(user => {
                    var li = document.createElement("li");
                    li.className = "user-item";
                    li.innerHTML = `
                        <div class="avatar">${getAvatarHtml(user)}</div>
                        <div class="flex-grow-1">
                            <h6 class="m-0 text-dark">${user.username}</h6>
                            <small class="text-muted">${user.email}</small>
                        </div>
                    `;
                    li.onclick = function() { selectUser(user, li); };
                    list.appendChild(li);
                });
            });
        }

        // 4. CHỌN NGƯỜI ĐỂ CHAT
        function selectUser(user, element) {
            document.querySelectorAll('.user-item').forEach(el => el.classList.remove('active'));
            element.classList.add('active');

            document.getElementById("chatWithTitle").innerText = user.username;
            var headerAvatar = document.getElementById("chatAvatar");
            headerAvatar.innerHTML = getAvatarHtml(user); 
            headerAvatar.style.display = "flex";
            
            document.getElementById("chatStatus").style.display = "block";
            document.getElementById("chatOptions").style.display = "block";

            currentRecipientEmail = user.email;
            document.getElementById("recipientEmail").value = user.email;

            document.getElementById("messageContent").disabled = false;
            document.getElementById("sendBtn").disabled = false;

            loadHistory(user.email);
        }

        // 5. TẢI LỊCH S
        function loadHistory(recipientEmail) {
            var historyDiv = document.getElementById("chat-history");
            
            // 1. Reset trạng thái
            currentArchivePage = 0;
            hasMoreArchive = true;
            isLoadingArchive = false;

            // 2. Hiện Loading
            historyDiv.innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';

            // 3. Lấy Email thật kỹ (Lấy từ hàm 1 sang để tránh lỗi)
            var senderEmail = document.getElementById("currentUserEmail").value;
            // Fallback: Nếu không lấy được từ DOM thì lấy từ biến toàn cục (nếu có)
            if (!senderEmail && typeof currentUserEmail !== 'undefined') {
                senderEmail = (typeof currentUserEmail === 'object') ? currentUserEmail.value : currentUserEmail;
            }

            if (!senderEmail) {
                console.error("❌ Lỗi: Không tìm thấy email người gửi!");
                return;
            }

            var url = '/messages/' + senderEmail + '/' + recipientEmail;

            fetch(url).then(res => res.json()).then(messages => {
                historyDiv.innerHTML = ''; 

                // 4. Render tin nhắn (Dùng cách tối ưu của hàm 2)
                var tempHtml = '';
                messages.forEach(msg => {
                    tempHtml += createMessageHTML(msg);
                });
                historyDiv.innerHTML = tempHtml;
                scrollToBottom();

                // 5. LOGIC QUAN TRỌNG (Lấy từ hàm 2): Tự động gọi Archive nếu Recent trống
                if (messages.length === 0 || historyDiv.scrollHeight <= historyDiv.clientHeight) {
                    console.log("⚠️ Recent trống hoặc ít -> Gọi cứu viện từ Archive!");
                    loadMoreArchives(recipientEmail);
                }

                // 6. Gắn sự kiện cuộn (Lấy từ hàm 2 nhưng sửa === 0 thành <= 10 cho nhạy)
                historyDiv.onscroll = function() {
                    if (historyDiv.scrollTop <= 10) {
                        loadMoreArchives(recipientEmail);
                    }
                };

            }).catch(err => {
                console.error(err);
                historyDiv.innerHTML = '<p class="text-center text-danger">Lỗi kết nối server.</p>';
            });
        }

        // 6. GỬI TIN NHẮN (Optimistic Update)
        function sendMessage() {
            var content = document.getElementById("messageContent").value.trim();
            var recipient = document.getElementById("recipientEmail").value;
            if(content && recipient) {
                // Gửi Server
                stompClient.send("/app/chat", {}, JSON.stringify({'recipientEmail': recipient, 'content': content}));
                
                // Hiện ngay lập tức cho mình xem
                var now = new Date();
                displayMessage({ senderId: myUUID, content: content, createdAt: now });
                
                document.getElementById("messageContent").value = '';
                document.getElementById("messageContent").focus();
                scrollToBottom();
            }
        }

        // 7. HÀM HIỂN THỊ TIN NHẮN (UI)
        function displayMessage(message) {
            var historyDiv = document.getElementById("chat-history");
            if (historyDiv.querySelector('.text-center')) historyDiv.innerHTML = '';

            var container = document.createElement('div');
            container.className = "message-container";
            var bubble = document.createElement('div');
            var timeStr = formatTime(message.createdAt);

            // Logic so sánh: Nếu là ID của mình -> Bên phải, Xanh
            if (String(message.senderId) === String(myUUID)) {
                container.style.justifyContent = "flex-end"; 
                bubble.className = "message my-message";
            } else {
                // Tất cả tin khác -> Bên trái, Xám (KHÔNG KIỂM TRA NGƯỜI GỬI NỮA ĐỂ TEST)
                container.style.justifyContent = "flex-start"; 
                bubble.className = "message other-message";
            }

            bubble.innerHTML = `<span>${message.content}</span><span class="timestamp">${timeStr}</span>`;
            container.appendChild(bubble);
            historyDiv.appendChild(container);
            scrollToBottom();
        }

        function scrollToBottom() {
            var d = document.getElementById("chat-history");
            d.scrollTop = d.scrollHeight;
        }

        // Thêm bạn & Chặn (Giữ nguyên)
        function addFriend() {
            var email = document.getElementById("addFriendEmail").value.trim();
            if(!email) return;
            fetch('/api/friends/add?email=' + email, {method:'POST'}).then(r=>r.text()).then(res=>{
                if(res==='ok') { alert("Đã thêm bạn!"); loadUsers(); document.getElementById("addFriendEmail").value=''; }
                else alert(res);
            });
        }
        function blockUser() {
            if(confirm("Chặn người này?")) {
                fetch('/api/friends/block?email='+currentRecipientEmail, {method:'POST'}).then(r=>r.text()).then(res=>{
                    if(res==='ok') location.reload();
                });
            }
        }
        // home.js

        function unfriendUser() {
            // 1. Lấy Email người đang chat cùng (được lưu trong thẻ hidden input)
            var targetEmail = document.getElementById("recipientEmail").value;

            if (!targetEmail) {
                alert("Chưa chọn người để hủy kết bạn!");
                return;
            }

            // 2. Hỏi xác nhận cho chắc ăn
            if (!confirm("Bạn có chắc chắn muốn hủy kết bạn với " + targetEmail + " không?")) {
                return;
            }

            // 3. Gọi API xóa
            fetch(`/api/friends/unfriend?targetEmail=${targetEmail}`, {
                method: 'POST'
            })
            .then(response => {
                if (response.ok) {
                    alert("Đã hủy kết bạn.");
                    
                    // 4. Xóa giao diện chat hiện tại
                    document.getElementById("chat-history").innerHTML = '';
                    document.getElementById("chatWithTitle").innerText = "Hãy chọn một người bạn...";
                    document.getElementById("chatAvatar").style.display = "none";
                    document.getElementById("recipientEmail").value = ""; // Xóa email đang chọn
                    document.getElementById("chatOptions").style.display = "none"; // Ẩn menu 3 chấm

                    // 5. Tải lại danh sách bạn bè bên trái
                    loadUsers(); 
                } else {
                    alert("Lỗi khi hủy kết bạn.");
                }
            })
            .catch(err => console.error(err));
        }

        document.getElementById("messageContent").addEventListener("keypress", function(e) { if(e.key==="Enter") sendMessage(); });

        connect();
        loadUsers();
        

        // --- 1. TẢI DANH SÁCH CHẶN ---
        function loadBlockedUsers() {
            const listContainer = document.getElementById("blockedList");
            listContainer.innerHTML = '<li class="list-group-item text-center py-4"><div class="spinner-border text-primary"></div></li>';

            // Gọi API lấy danh sách chặn (Bạn cần viết API này bên Java Controller)
            fetch('/api/users/blocked') 
                .then(response => response.json())
                .then(users => {
                    listContainer.innerHTML = ''; // Xóa icon loading

                    if (users.length === 0) {
                        listContainer.innerHTML = '<li class="list-group-item text-center text-muted py-4">Bạn chưa chặn ai cả.</li>';
                        return;
                    }

                    users.forEach(user => {
                        const li = document.createElement("li");
                        li.className = "list-group-item d-flex justify-content-between align-items-center px-4 py-3";
                        
                        // Xử lý Avatar (nếu null thì lấy chữ cái đầu)
                        let avatarHtml = '';
                        if (user.avatarUrl) {
                            avatarHtml = `<img src="/uploads/${user.avatarUrl}" class="rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">`;
                        } else {
                            const firstLetter = user.username.charAt(0).toUpperCase();
                            avatarHtml = `<div class="rounded-circle bg-secondary text-white d-flex align-items-center justify-content-center" style="width: 40px; height: 40px; font-weight: bold;">${firstLetter}</div>`;
                        }

                        li.innerHTML = `
                            <div class="d-flex align-items-center">
                                ${avatarHtml}
                                <div class="ms-3">
                                    <h6 class="m-0 fw-bold">${user.username}</h6>
                                    <small class="text-muted">${user.email}</small>
                                </div>
                            </div>
                            <button onclick="unblockUser('${user.id}')" class="btn btn-sm btn-outline-primary fw-bold">
                                Bỏ chặn
                            </button>
                        `;
                        listContainer.appendChild(li);
                    });
                })
                .catch(err => {
                    console.error(err);
                    listContainer.innerHTML = '<li class="list-group-item text-center text-danger">Lỗi tải dữ liệu</li>';
                });
        }

        // --- 2. XỬ LÝ BỎ CHẶN ---
        function unblockUser(targetUserId) {
            if(!confirm("Bạn có chắc muốn bỏ chặn người này?")) return;

            fetch(`/api/friends/unblock?targetId=${targetUserId}`, {
                method: 'POST'
            })
            .then(response => {
                if (response.ok) {
                    // Reload lại danh sách chặn để thấy người đó biến mất
                    loadBlockedUsers(); 
                    // Reload lại danh sách bạn bè bên ngoài (nếu cần)
                    // loadFriends(); 
                    alert("Đã bỏ chặn thành công!");
                } else {
                    alert("Lỗi khi bỏ chặn.");
                }
            })
            .catch(err => console.error(err));
        }


        // --- BIẾN TOÀN CỤC ---
        var currentArchivePage = 0;
        var isLoadingArchive = false;
        var hasMoreArchive = true;

        // --- 1. TẢI LỊCH SỬ (Mới vào) ---
        
        function loadMoreArchives(recipientEmail) {
            if (isLoadingArchive || !hasMoreArchive) return;
                
            isLoadingArchive = true;
            var historyDiv = document.getElementById("chat-history");
                
                // Thêm loading nhỏ ở trên cùng
            var loader = document.createElement("div");
            loader.id = "archive-loader";
            loader.className = "text-center my-2";
            loader.innerHTML = '<div class="spinner-border spinner-border-sm text-secondary"></div>';
            historyDiv.prepend(loader);

            var senderEmail = document.getElementById("currentUserEmail").value;
            var url = `/api/messages/archive?senderId=${senderEmail}&recipientId=${recipientEmail}&page=${currentArchivePage}`;

            fetch(url).then(res => res.json()).then(messages => {
                    // Xóa loading
                var loaderEl = document.getElementById("archive-loader");
                if(loaderEl) loaderEl.remove();

                if (messages.length === 0) {
                    hasMoreArchive = false;
                    isLoadingArchive = false;
                    return;
                }

                var oldHeight = historyDiv.scrollHeight;
                var tempDiv = document.createElement('div');
                    
            // 1. UNCOMMENT ĐOẠN NÀY ĐỂ HIỂN THỊ TIN NHẮN
                messages.forEach(msg => {
                    tempDiv.innerHTML += createMessageHTML(msg);
                });
                    
                    // Chèn vào đầu
                    historyDiv.insertAdjacentHTML('afterbegin', tempDiv.innerHTML);
                    
                    // Giữ vị trí cuộn
                    historyDiv.scrollTop = historyDiv.scrollHeight - oldHeight;

                    currentArchivePage++;
                    isLoadingArchive = false;
                    
                    // 2. LOGIC TỰ ĐỘNG TẢI TIẾP (Đặt ở cuối cùng)
                    // Sau khi đã in tin nhắn ra rồi, mà thấy màn hình vẫn còn trống (chưa có thanh cuộn)
                    // Thì mới gọi đệ quy để tải trang tiếp theo luôn
                    if (hasMoreArchive && historyDiv.scrollHeight <= historyDiv.clientHeight) {
                        console.log("Vẫn chưa đầy màn hình -> Tải tiếp trang sau...");
                        loadMoreArchives(recipientEmail);
                    }

                    if (messages.length < 20) {
                        hasMoreArchive = false;
                        var endMsg = document.createElement("div");
                        endMsg.className = "text-center text-muted small my-3";
                        endMsg.innerText = "--- Bắt đầu cuộc trò chuyện ---";
                        historyDiv.prepend(endMsg);
                    }
                }).catch(err => {
                    console.error(err);
                    isLoadingArchive = false;
                    var loaderEl = document.getElementById("archive-loader");
                    if(loaderEl) loaderEl.remove();
                });
        }

        // --- 3. HÀM TẠO HTML TIN NHẮN (Đã sửa chuẩn) ---
        // Hàm này dùng chung cho cả Tin nhắn mới, Tin nhắn cũ và Socket
        function createMessageHTML(msg) {
            var currentUserId = document.getElementById("currentUserId").value;
            var isMe = (msg.senderId === currentUserId);
            
            var alignClass = isMe ? "my-message" : "other-message";
            var containerClass = isMe ? "justify-content-end" : "justify-content-start";
            
            // ✅ SỬA QUAN TRỌNG: Dùng formatTime để giờ đẹp (09:05)
            var timeStr = formatTime(msg.createdAt);

            return `
                <div class="message-container ${containerClass}">
                    <div class="message ${alignClass}">
                        ${msg.content}
                        <span class="timestamp">${timeStr}</span>
                    </div>
                </div>
            `;
        }

        // --- 4. HÀM FORMAT GIỜ (Chuẩn) ---
        function formatTime(dateString) {
            if (!dateString) return "";
            var date = new Date(dateString);
            var hours = date.getHours().toString().padStart(2, '0');
            var minutes = date.getMinutes().toString().padStart(2, '0');
            return hours + ":" + minutes;
        }


