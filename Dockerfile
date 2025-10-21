FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/FutureXCourseCatalog-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]