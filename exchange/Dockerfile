FROM openjdk:23-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9084

ENTRYPOINT ["java", "-jar", "app.jar"]
