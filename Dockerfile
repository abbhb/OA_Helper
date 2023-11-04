FROM openjdk:17-jdk-alpine
LABEL authors="qc200"

WORKDIR /app
COPY helper-custom-server/target/helper-custom-server-1.0-SNAPSHOT.jar /app

CMD ["java", "-jar", "helper-custom-server-1.0-SNAPSHOT.jar"]