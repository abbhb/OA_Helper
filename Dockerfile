FROM openjdk:17-jdk-alpine
LABEL authors="qc200"

WORKDIR /app
RUN apk --update add tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata && \
    rm -rf /var/cache/apk/* \


COPY helper-custom-server/target/helper-custom-server-1.0-SNAPSHOT.jar /app

CMD ["java", "-jar", "helper-custom-server-1.0-SNAPSHOT.jar"]