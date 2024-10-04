FROM openjdk:17-ea-13-oraclelinux7
LABEL authors="qc200"

WORKDIR /app

COPY rocketmq-exporter-self.jar /app


EXPOSE 18454


CMD ["java", "-jar","-Duser.timezone=Asia/Shanghai", "rocketmq-exporter-self.jar","--rocketmq.config.namesrvAddr=192.168.12.12:9876","--server.port=18454"]