FROM openjdk:21-slim
EXPOSE 8080
WORKDIR /app
COPY target/smit-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]