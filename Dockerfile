FROM openjdk:17-jdk-alpine
LABEL authors="qc200"

WORKDIR /app

COPY helper-custom-server/target/helper-custom-server-1.0-SNAPSHOT.jar /app
RUN apk --update add tzdata
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone

CMD ["java", "-jar","-Duser.timezone=Asia/Shanghai", "helper-custom-server-1.0-SNAPSHOT.jar"]