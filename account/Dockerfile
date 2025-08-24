FROM openjdk:23-jdk-slim

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9081

ENTRYPOINT ["java", "-jar", "app.jar"]
