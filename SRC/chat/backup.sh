
#!/bin/bash
# Script Backup Tự Động (Chương 7)
TIMESTAMP=$(date +"%Y%m%d")
echo "Đang backup PostgreSQL..."
docker exec chat_postgres pg_dump -U postgres userChat > backup_pg_$TIMESTAMP.sql
echo "Đang backup MongoDB..."
docker exec chat_mongo mongodump --out backup_mongo_$TIMESTAMP
echo "Xong!"