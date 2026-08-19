FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y libfreetype6 fontconfig && rm -rf /var/lib/apt/lists/*

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/myapp.jar"]