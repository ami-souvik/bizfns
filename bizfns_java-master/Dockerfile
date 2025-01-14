# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app

# Use the spring-boot-maven-plugin to build the application
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]