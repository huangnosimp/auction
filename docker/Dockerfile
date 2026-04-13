FROM eclipse-temurin:25-jre-alpine
LABEL authors="vuhoa"
WORKDIR /app
COPY server.jar /app/server.jar

ENTRYPOINT ["java", "-Xmx300m", "-jar", "server.jar"]