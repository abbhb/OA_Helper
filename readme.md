# AI综合服务平台



### Version：1.8.0

## 环境准备
- rabbitmq 最新版 加上延迟队列插件 推荐docker安装 单体文件太多
- redis 最新版即可
- mysql8.0.20
- minio 最新版即可


- [AI服务平台主后端](https://github.com/abbhb/Springboot-PrinterSharing)
- [AI服务平台前端(本项目也是其中一部分的后端)](https://github.com/abbhb/Vue-PrinterSharing)
- [AI服务平台DOC后端](https://github.com/abbhb/Printer-Doc)

## 运行

直接maven拉一下依赖
运行即可

```text
注意:如果是java8以上版本需要加上VM参数,不然可能出现报错
一些反射找不到(但我现在没加也没报错)
```

#### 权限规定

list为菜单权限整个页面批量数据权限
query为单个数据权限 结合角色数据限制来查询
add 添加权限
delete 删除权限
update 修改权限

## 注意:

+ swagger在上线前最好关掉 在主函数上注释掉注解即可
+ minio 报连接错误可能是时区问题，minio需要美国1时区
+ token 存在cookies 方便管理
+ 避坑：mysql8 key value不能作为字段名

## 初始化项目：【必须】

创建superadmin角色,id必须为1L
创建一个用户名为admin的用户，为该用户绑定1L的角色
