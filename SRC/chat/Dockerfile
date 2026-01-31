# Giai đoạn 1: Dùng Maven để Build ra file .jar
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Lệnh build (bỏ qua test cho nhanh)
RUN mvn clean package -DskipTests

# Giai đoạn 2: Dùng Java để chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy file .jar từ giai đoạn 1 sang
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]