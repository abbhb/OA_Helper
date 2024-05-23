# 暂不使用这个服务了

# 此服务为任务调度中心的服务

必须先启动这个，也是个中间件服务！
通过docker-compose.yml可以快速构建容器

## 复制到文件服务器执行就行

## 复制到文件服务器data目录下创建一个xxjob目录下面执行即可!

在docker-compose.yml 的同级目录下执行命令启动容器

`docker-compose up -d `

## 注意：部署成功后，访问ip+端口是正常404

## 得访问http://192.168.12.12:18080/xxl-job-admin/

后面得加上 /xxl-job-admin/

# 默认用户名admin密码123456

然后启动的时候设置的那个accesstoken跟后面客户端调用的时候会用上