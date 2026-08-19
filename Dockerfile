# 第一阶段：构建
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B  # 下载依赖（利用缓存）
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y libfreetype6 fontconfig && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/myapp.jar .

EXPOSE 8080

CMD ["java", "-jar", "myapp.jar"]