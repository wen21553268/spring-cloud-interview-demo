环境要求
JDK 17
Maven 3.6+
MySQL 8.0
Docker (可选，用于容器化运行)

快速启动（本地运行）
配置中心

cd spring-product-config-server/target

java -jar spring-product-config-server-4.0.1.jar

端口: 8088

cd spring-product-discovery-server/target

java -jar spring-product-discovery-server-4.0.1.jar

端口: 8089

cd spring-product-products-service/target

java -jar spring-product-products-service-4.0.1.jar

端口: 8090

cd spring-product-api-gateway/target

java -jar spring-product-api-gateway-4.0.1.jar

端口: 7573

数据库初始化
sql
CREATE DATABASE IF NOT EXISTS product_db;
USE product_db;

普通用户 (USER角色)

curl -X POST http://localhost:7573/auth/login -H "Content-Type: application/json" -d "{\"username\":\"user_1\",\"password\":\"user_1\"}"

输出示例：
    {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzEiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzcwOTgwMDAwLCJleHAiOjE3NzA5ODM2MDB9.xxx",
        "username": "user_1",
        "roles": ["USER"]
    }

EDITOR角色

curl -X POST http://localhost:7573/auth/login -H "Content-Type: application/json" -d "{\"username\":\"editor_1\",\"password\":\"editor_1\"}"

输出示例：
    {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZGl0b3JfMSIsInJvbGVzIjpbIlJPTEVfRURJVE9SIiwiUk9MRV9VU0VSIl0sImlhdCI6MTc3MDk4MDAwMCwiZXhwIjoxNzcwOTgzNjAwfQ.xxx",
        "username": "editor_1",
        "roles": ["EDITOR", "USER"]
    }

管理员 (PRODUCT_ADMIN角色)

curl -X POST http://localhost:7573/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"adm_1","password":"adm_1"}'

输出示例：
    {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1fMSIsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX0VESVRPUiIsIlJPTEVfVVNFUiJdLCJpYXQiOjE3NzA5ODAwMDAsImV4cCI6MTc3MDk4MzYwMH0.xxx",
        "username": "adm_1",
        "roles": ["PRODUCT_ADMIN", "EDITOR", "USER"]
    }

GitHub OAuth2 登录
浏览器访问以下地址，授权后自动返回 JSON 格式的 token：
http://localhost:7573/auth/github/login

产品管理 API（需携带 ACCESS TOKEN）
将 YOUR_TOKEN 替换为上面登录获取的 token。

添加产品

curl -X POST http://localhost:7573/products/create -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d "{\"name\":\"测试产品A\"}"

输出示例：
    {
        "id": 1,
        "name": "测试产品A"
    }

查询所有产品

curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3ZW4yMTU1MzI2OCIsInJvbGVzIjpbIlBST0RVQ1RfQURNSU4iLCJFRElUT1IiLCJVU0VSIl0sImlhdCI6MTc3MDk5NzE4NywiZXhwIjoxNzcxMDAwNzg3fQ.3qUu94zGJ6JOBcoyEP4knA4CzHY8N50FZLtbqjwfumw" http://localhost:7573/products/get

输出示例：
    [
        {
        "id": 1,
        "name": "测试产品A"
        }
    ]

修改产品

curl -X POST http://localhost:7573/products/update/1 -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3ZW4yMTU1MzI2OCIsInJvbGVzIjpbIlBST0RVQ1RfQURNSU4iLCJFRElUT1IiLCJVU0VSIl0sImlhdCI6MTc3MDk5NzE4NywiZXhwIjoxNzcxMDAwNzg3fQ.3qUu94zGJ6JOBcoyEP4knA4CzHY8N50FZLtbqjwfumw" -d "{\"name\":\"修改后的产品\"}"

输出示例：
    {
        "id": 1,
        "name": "修改后的产品"
    }

删除产品

curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3ZW4yMTU1MzI2OCIsInJvbGVzIjpbIlBST0RVQ1RfQURNSU4iLCJFRElUT1IiLCJVU0VSIl0sImlhdCI6MTc3MDk5NzE4NywiZXhwIjoxNzcxMDAwNzg3fQ.3qUu94zGJ6JOBcoyEP4knA4CzHY8N50FZLtbqjwfumw" http://localhost:7573/products/delete/1

# 打包所有服务
mvn clean package -DskipTests

# 启动容器
docker-compose up -d

# 查看日志
docker-compose logs -f

访问地址：

配置中心: http://localhost:8088

注册中心: http://localhost:8089

网关: http://localhost:7573

启动顺序必须为：config-server → discovery-server → 其他服务

MySQL 需要提前创建 product_db 数据库

所有服务使用自定义端口（8088/8089/8090/7573），互不冲突

GitHub OAuth2 需要配置真实的 client-id/secret，当前已配置测试账号