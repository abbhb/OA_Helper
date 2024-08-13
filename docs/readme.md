# 模块
## 用户


## 公共模块
### core 各种常量和各个公共模块 异常的定义等等 公共工具
### data-scope
### log 整合统一的日志
### redis 配置和服务
### seata分布式事务
### 安全（auth，权限+认证）
### swagger
### 基于netty的websocket（以前的chat模块，直接抽象出来作为公共的组件）
## gateway网关
### 
只拆一个common的模块，真只是公共service组件才拆，不然互相依赖很麻烦