FROM eclipse-temurin:21-jdk

LABEL authors="aar"

WORKDIR /app

COPY build/libs/telegram-bot-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
