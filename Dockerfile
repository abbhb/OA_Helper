FROM openjdk:17-ea-13-oraclelinux7
LABEL authors="qc200"
ARG minioaccessKey
ARG miniosecretKey
ARG emailpassword
ARG mysqlpassword

WORKDIR /app

COPY helper-custom-server/target/helper-custom-server-1.0-SNAPSHOT.jar /app
#RUN apk --update add tzdata
#RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#RUN echo "Asia/Shanghai" > /etc/timezone
# 注意映射这三个端口，必须外网也是这三
EXPOSE 55550
EXPOSE 8090
EXPOSE 9999
ENV emailpassword=${emailpassword}
ENV mysqlpassword=${mysqlpassword}
ENV minioaccessKey=${minioaccessKey}
ENV miniosecretKey=${miniosecretKey}

CMD ["java", "-jar","-Duser.timezone=Asia/Shanghai", "helper-custom-server-1.0-SNAPSHOT.jar","--helper.mysql.password=${mysqlpassword}","--helper.email.password=${emailpassword}","--helper.minio.accessKey=${minioaccessKey}","--helper.minio.secretKey=${miniosecretKey}"]