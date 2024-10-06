#!/bin/bash

# 检查是否传入了所有必要的参数
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 redispassword minioaccessKey miniosecretKey emailpassword mysqlpassword"
    exit 1
fi

# 获取传入的参数
redispassword=$1
minioaccessKey=$2
miniosecretKey=$3
emailpassword=$4
mysqlpassword=$5

# 创建或覆盖.env文件
echo "Creating or updating .env file with the provided environment variables..."

# 使用echo命令将变量写入.env文件
echo "REDIS_PASSWORD=$redispassword" > .env
echo "MINIO_ACCESS_KEY=$minioaccessKey" >> .env
echo "MINIO_SECRET_KEY=$miniosecretKey" >> .env
echo "EMAIL_PASSWORD=$emailpassword" >> .env
echo "MYSQL_PASSWORD=$mysqlpassword" >> .env

echo ".env file has been created or updated successfully."