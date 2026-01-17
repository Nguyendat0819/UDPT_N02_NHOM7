{/* <script>
        var stompClient = null;
        var myUUID = document.getElementById("currentUserId").value; 
        var currentRecipient = null; 

        // === HÀM MỚI: XỬ LÝ HIỂN THỊ AVATAR ===
        function getAvatarHtml(user) {
            // Kiểm tra nếu có avatarUrl
            if (user.avatarUrl && user.avatarUrl.trim() !== "") {
                var src = user.avatarUrl;
                
                // SỬA LẠI ĐÚNG Ở ĐÂY: Chỉ cần /uploads/ là đủ
                if (!src.startsWith("/") && !src.startsWith("http")) {
                     src = "/uploads/" + src; 
                }
                
                // Code hiển thị ảnh, nếu lỗi ảnh thì hiện chữ cái đầu
                return `<img src="${src}" class="avatar-img" onerror="this.parentNode.innerHTML='${user.username.charAt(0).toUpperCase()}'">`;
            } else {
                // Mặc định: Chữ cái đầu
                return user.username.charAt(0).toUpperCase();
            }
        }
        // =======================================

        function connect() {
            var socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null; 

            stompClient.connect({}, function (frame) {
                console.log('Connected to WebSocket');
                stompClient.subscribe('/user/queue/messages', function (messageOutput) {
                    var message = JSON.parse(messageOutput.body);
                    if (String(message.senderId) === String(myUUID) || 
                       (currentRecipient && message.senderId === currentRecipient) || 
                       (currentRecipient && document.getElementById("recipientEmail").value === currentRecipient)) { 
                         displayMessage(message);
                    }
                });
            });
        }

        // 2. LẤY DANH SÁCH BẠN BÈ (CẬP NHẬT LOGIC ẢNH)
        function loadUsers() {
            fetch('/api/users')
                .then(response => response.json())
                .then(users => {
                    var list = document.getElementById("userList");
                    list.innerHTML = ""; 
                    
                    if (users.length === 0) {
                        list.innerHTML = '<li class="text-center mt-3 text-muted"><small>Chưa có bạn bè.<br>Hãy thêm bạn mới!</small></li>';
                        return;
                    }

                    users.forEach(user => {
                        var li = document.createElement("li");
                        li.className = "user-item";
                        
                        // GỌI HÀM LẤY AVATAR MỚI
                        var avatarContent = getAvatarHtml(user);
                        
                        li.innerHTML = `
                            <div class="avatar">${avatarContent}</div>
                            <div class="flex-grow-1">
                                <h6 class="m-0 text-dark">${user.username}</h6>
                                <small class="text-muted" style="font-size: 0.8rem;">${user.email}</small>
                            </div>
                            <i class="fa-solid fa-chevron-right text-muted" style="font-size: 0.8rem;"></i>
                        `;
                        
                        li.onclick = function() { selectUser(user, li); };
                        list.appendChild(li);
                    });
                });
        }

        // 3. CHỌN NGƯỜI ĐỂ CHAT (CẬP NHẬT HEADER)
        function selectUser(user, element) {
            document.querySelectorAll('.user-item').forEach(el => el.classList.remove('active'));
            element.classList.add('active');

            document.getElementById("chatWithTitle").innerText = user.username;
            
            // CẬP NHẬT AVATAR TRÊN HEADER (Dùng hàm mới)
            var headerAvatar = document.getElementById("chatAvatar");
            headerAvatar.innerHTML = getAvatarHtml(user); 
            headerAvatar.style.display = "flex";
            
            document.getElementById("chatStatus").style.display = "block";
            document.getElementById("chatOptions").style.display = "block";

            currentRecipient = user.email;
            document.getElementById("recipientEmail").value = user.email;

            document.getElementById("messageContent").disabled = false;
            document.getElementById("sendBtn").disabled = false;

            loadHistory(user.email);
        }

        function loadHistory(recipientEmail) {
            var historyDiv = document.getElementById("chat-history");
            historyDiv.innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';

            fetch('/history/' + recipientEmail)
                .then(response => response.json())
                .then(messages => {
                    historyDiv.innerHTML = ''; 
                    if(messages.length === 0) {
                        historyDiv.innerHTML = '<div class="text-center mt-5 text-muted"><p>Chưa có tin nhắn nào.<br>Hãy nói "Xin chào" 👋</p></div>';
                    }
                    messages.forEach(msg => displayMessage(msg));
                    scrollToBottom();
                });
        }

        function sendMessage() {
            var content = document.getElementById("messageContent").value.trim();
            var recipient = document.getElementById("recipientEmail").value;

            if(content && recipient) {
                var chatRequest = {
                    'recipientEmail': recipient,
                    'content': content
                };
                stompClient.send("/app/chat", {}, JSON.stringify(chatRequest));
                document.getElementById("messageContent").value = ''; 
                document.getElementById("messageContent").focus();
            }
        }

        function displayMessage(message) {
            var historyDiv = document.getElementById("chat-history");
            if (historyDiv.querySelector('.text-center.mt-5')) {
                historyDiv.innerHTML = '';
            }

            var container = document.createElement('div');
            container.className = "message-container";

            var bubble = document.createElement('div');
            var time = message.createdAt ? new Date(message.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : '';

            if (String(message.senderId) === String(myUUID)) {
                container.style.justifyContent = "flex-end"; 
                bubble.className = "message my-message";
            } else {
                container.style.justifyContent = "flex-start"; 
                bubble.className = "message other-message";
            }

            bubble.innerHTML = `
                <span>${message.content}</span>
                <span class="timestamp">${time}</span>
            `;

            container.appendChild(bubble);
            historyDiv.appendChild(container);
            scrollToBottom();
        }

        function addFriend() {
            var email = document.getElementById("addFriendEmail").value.trim();
            if(!email) { alert("Vui lòng nhập email!"); return; }

            fetch('/api/friends/add?email=' + email, { method: 'POST' })
                .then(response => response.text())
                .then(result => {
                    if (result === "ok") {
                        alert("Đã thêm bạn thành công!");
                        document.getElementById("addFriendEmail").value = "";
                        loadUsers(); 
                    } else {
                        alert("Lỗi: " + result);
                    }
                });
        }

        function blockUser() {
            if (!currentRecipient) return;
            if (confirm("Bạn chắc chắn muốn chặn người này? Họ sẽ không thể nhắn tin cho bạn.")) {
                fetch('/api/friends/block?email=' + currentRecipient, { method: 'POST' })
                    .then(response => response.text())
                    .then(result => {
                        if (result === "ok") {
                            alert("Đã chặn người dùng này.");
                            location.reload(); 
                        } else {
                            alert("Lỗi: " + result);
                        }
                    });
            }
        }
        
        function unfriendUser() {
             if (confirm("Bạn muốn hủy kết bạn?")) {
                alert("Tính năng đang phát triển!");
            }
        }

        function scrollToBottom() {
            var historyDiv = document.getElementById("chat-history");
            historyDiv.scrollTop = historyDiv.scrollHeight;
        }

        document.getElementById("messageContent").addEventListener("keypress", function(event) {
            if (event.key === "Enter") { sendMessage(); }
        });

        connect();
        loadUsers();

    </script> */}





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

        // 5. TẢI LỊCH SỬ
        function loadHistory(recipientEmail) {
            var historyDiv = document.getElementById("chat-history");
            historyDiv.innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';
            fetch('/history/' + recipientEmail).then(res => res.json()).then(messages => {
                historyDiv.innerHTML = '';
                if(messages.length === 0) historyDiv.innerHTML = '<p class="text-center mt-5 text-muted">Chưa có tin nhắn nào.</p>';
                messages.forEach(msg => displayMessage(msg));
                scrollToBottom();
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
            var time = message.createdAt ? new Date(message.createdAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'}) : '';

            // Logic so sánh: Nếu là ID của mình -> Bên phải, Xanh
            if (String(message.senderId) === String(myUUID)) {
                container.style.justifyContent = "flex-end"; 
                bubble.className = "message my-message";
            } else {
                // Tất cả tin khác -> Bên trái, Xám (KHÔNG KIỂM TRA NGƯỜI GỬI NỮA ĐỂ TEST)
                container.style.justifyContent = "flex-start"; 
                bubble.className = "message other-message";
            }

            bubble.innerHTML = `<span>${message.content}</span><span class="timestamp">${time}</span>`;
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
        function unfriendUser() { alert("Coming soon!"); }

        document.getElementById("messageContent").addEventListener("keypress", function(e) { if(e.key==="Enter") sendMessage(); });

        connect();
        loadUsers();
    