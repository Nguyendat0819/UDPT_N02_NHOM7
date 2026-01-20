#!/bin/bash

# 1. Cấu hình
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
mkdir -p $BACKUP_DIR

echo " [$(date)] Bắt đầu backup (Phiên bản Direct Stream)..."

# ---------------------------------------------------------
# 2. BACKUP POSTGRESQL (Đã ngon lành)
# ---------------------------------------------------------
echo " [1/3] Đang backup PostgreSQL..."
MSYS_NO_PATHCONV=1 docker exec postgres-db pg_dump -U postgres userChat > $BACKUP_DIR/postgres_users_$TIMESTAMP.sql

# ---------------------------------------------------------
# 3. BACKUP MONGODB (SỬA ĐỔI: Dùng > thay vì docker cp)
# Cách này giúp Git Bash tự tạo file, đảm bảo 100% tìm thấy file
# ---------------------------------------------------------
echo " [2/3] Đang backup MongoDB..."
# --archive không có giá trị (=) sẽ tự động đẩy ra màn hình, ta dùng > để hứng vào file
MSYS_NO_PATHCONV=1 docker exec mongo-db mongodump --db personChat --archive > $BACKUP_DIR/mongo_chat_$TIMESTAMP.archive

# ---------------------------------------------------------
# 4. NÉN TOÀN BỘ
# ---------------------------------------------------------
echo " [3/3] Đang nén file tổng..."
tar -czvf $BACKUP_DIR/FULL_BACKUP_$TIMESTAMP.tar.gz \
    $BACKUP_DIR/postgres_users_$TIMESTAMP.sql \
    $BACKUP_DIR/mongo_chat_$TIMESTAMP.archive

# ---------------------------------------------------------
# 5. DỌN DẸP
# ---------------------------------------------------------
rm $BACKUP_DIR/postgres_users_$TIMESTAMP.sql
rm $BACKUP_DIR/mongo_chat_$TIMESTAMP.archive

echo " [$(date)] BACKUP THÀNH CÔNG! File tại: $BACKUP_DIR/FULL_BACKUP_$TIMESTAMP.tar.gz"