# spring cloud 
-------
## 使用示例参考 [例子](https://github.com/21guns/spring-cloud-study)

## 注册中心
---------
1. eureka
    启动该spring boot项目
2. consul
    启动consul可以使用docker启动，命令
    `docker run -p 8400:8400 -p 8500:8500 -p 8600:53/udp -h node1 -d progrium/consul -server -bootstrap  -ui-dir /ui`


## api网关
---------
配置信息参看api-gateway项目中配置文件

## limiting模块实现降级、限流、滚动、灰度、AB、金丝雀等等等等 [1](http://xujin.org/sc/sc-ribbon-demoted/) [2](http://www.jianshu.com/p/37ee1e84900a)
---------
### eureka
 - 修改metadata
    http://localhost:8761/eureka/apps/MICROSERVICE-PROVIDER-USER/68c08d456a26:microservice-provider-user:8081/metadata?weight=200

### consul
   目前consul service不支持metadata，使用tags进行设置，要想修改tags需要设置EnableTagOverride为true，目前spring cloud consul未提供EnableTagOverride的配置方式：
   * 1.使用如下命令将EnableTagOverride设置true
   
    curl \
    --request PUT \
    --data '{
        "ID": "microservice-provider-user-docker-8081",
         "Name": "microservice-provider-user",
		  "Tags": [
		    "weight=55"
		  ],
		 "Address": "192.168.2.177",
		  "Port": 8081,
		  "EnableTagOverride": true

		}' \
    http://localhost:8500/v1/agent/service/register
    * 2.更行tags
    https://www.consul.io/api/catalog.html#catalog_register
    
    json文件`{
        "ID": "e5624ba1-7aaa-d035-45b9-1e2aee06c2b6",
         "Node": "d9b621972724",
         "Address": "127.0.0.1",
         "Datacenter": "dc1",
         "TaggedAddresses": {
             "lan": "127.0.0.1",
             "wan": "127.0.0.1"
         },
         "NodeMeta": {"somekey": "somevalue"},
         "Service": {
           "ID": "microservice-provider-user-docker-8081",
           "Service": "microservice-provider-user",
           "Tags": [
               "weight=2"
           ],
           "EnableTagOverride": true, #可以覆盖tag
           "Address": "192.168.2.177",
           "Port": 8081
         },
         "Check": {
           "Node": "d9b621972724",
           "CheckID": "service:microservice-provider-user-docker-8081",
           "Name": "Redis health check",
           "Notes": "Script based health check",
           "Status": "passing",
           "ServiceID": "microservice-provider-user-docker-8081"
         }
       }`
       
    执行命令：
        `curl \
              --request PUT \
              --data @payload.json \
              http://localhost:8500/v1/catalog/register`
