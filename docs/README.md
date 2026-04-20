# 寻宝电商系统 - 需求文档

## 一、项目概述

寻宝 (xunbao) 是一个面向 AI 智能体（Agent）设计的电商系统，提供标准化的 RESTful API 和 CLI 接口，支持智能体完成商品浏览、下单、发货等电商核心流程。

系统采用前后端分离架构，便于智能体进行 HTTP 调用和自动化操作。后续将持续扩展更多电商能力。

包含以下组件：

| 组件 | 技术栈 | 说明 |
|------|--------|------|
| **backend** | Java 17 + Spring Boot | RESTful API 后端服务 |
| **buyer** | Go + Cobra | 买家命令行客户端 |
| **merchant** | Go + Cobra | 商家命令行客户端 |
| **oss** | Docker MinIO | 对象存储服务 |

---

## 二、环境配置

### 2.1 后端配置

**文件**: `backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://192.168.31.81:5432/xunbao
    username: xunbao
    password: xunbao123
  jpa:
    hibernate:
      ddl-auto: none

jwt:
  secret: your-256-bit-secret-key-for-jwt-token-signing-must-be-long-enough
  access-token-expiration: 900000
  refresh-token-expiration: 604800000

oss:
  endpoint: http://192.168.31.81:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket: xunbao-images
```

### 2.2 JDK 要求

**要求**: JDK 17+

**本机路径**: `C:\Program Files\OpenJDK\jdk-17.0.13+11`

```bash
# Mac/Linux
export JAVA_HOME=/path/to/jdk-17

# Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\OpenJDK\jdk-17.0.13+11"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

# 验证
java -version
```

### 2.3 启动后端

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

---

## 三、API 接口文档

### 3.1 认证模块 `/api/v1/auth`

| 方法 | 路径 | 说明 | 认证 | 请求体 |
|------|------|------|------|--------|
| POST | `/register` | 用户注册 | 否 | RegisterRequest |
| POST | `/login` | 用户登录 | 否 | LoginRequest |
| POST | `/refresh` | 刷新Token | 否 | RefreshTokenRequest |

**RegisterRequest:**
```json
{
  "email": "user@test.com",
  "password": "123456",
  "role": "BUYER"  // 或 "MERCHANT"
}
```

**LoginRequest:**
```json
{
  "email": "user@test.com",
  "password": "123456"
}
```

**AuthResponse:**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": 1,
    "email": "user@test.com",
    "role": "BUYER"
  }
}
```

### 3.2 商品模块 `/api/v1/products`

| 方法 | 路径 | 说明 | 认证 | 参数 |
|------|------|------|------|------|
| GET | `/` | 搜索商品 | 否 | keyword, categoryId, minPrice, maxPrice, inStock, page, size |
| GET | `/{id}` | 获取商品详情 | 否 | - |
| POST | `/` | 创建商品 | MERCHANT | CreateProductRequest |
| PUT | `/{id}` | 更新商品 | MERCHANT | UpdateProductRequest |
| DELETE | `/{id}` | 删除商品 | MERCHANT | - |
| PUT | `/{id}/image` | 上传商品图片 | MERCHANT | MultipartFile |
| GET | `/{id}/variants` | 获取商品变体 | 否 | - |
| POST | `/{id}/variants` | 添加商品变体 | MERCHANT | CreateVariantRequest |
| GET | `/{id}/reviews` | 获取商品评价 | 否 | page, size |
| POST | `/{id}/reviews` | 添加商品评价 | 登录用户 | CreateReviewRequest |

### 3.3 订单模块 `/api/v1/orders`

| 方法 | 路径 | 说明 | 认证 | 参数 |
|------|------|------|------|------|
| GET | `/` | 获取订单列表 | 登录用户 | page, size |
| GET | `/{id}` | 获取订单详情 | 登录用户 | - |
| POST | `/` | 创建订单 | BUYER | CreateOrderRequest |
| PUT | `/{id}/status` | 更新订单状态 | MERCHANT | status |
| PUT | `/{id}/express` | 更新物流信息 | MERCHANT | UpdateExpressRequest |

**订单状态**: `PENDING` -> `PAID` -> `SHIPPED` -> `COMPLETED` / `CANCELLED`

### 3.4 购物车模块 `/api/v1/cart`

| 方法 | 路径 | 说明 | 认证 | 参数/请求体 |
|------|------|------|------|-------------|
| GET | `/` | 获取购物车列表 | 登录用户 | - |
| POST | `/` | 添加商品到购物车 | 登录用户 | AddCartRequest |
| PUT | `/{id}` | 更新购物车数量 | 登录用户 | UpdateCartRequest |
| DELETE | `/{id}` | 移除购物车商品 | 登录用户 | - |
| DELETE | `/` | 清空购物车 | 登录用户 | - |

### 3.5 地址模块 `/api/v1/addresses`

| 方法 | 路径 | 说明 | 认证 | 参数/请求体 |
|------|------|------|------|-------------|
| GET | `/` | 获取地址列表 | 登录用户 | - |
| POST | `/` | 创建地址 | 登录用户 | CreateAddressRequest |
| PUT | `/{id}` | 更新地址 | 登录用户 | CreateAddressRequest |
| DELETE | `/{id}` | 删除地址 | 登录用户 | - |
| PUT | `/{id}/default` | 设置默认地址 | 登录用户 | - |

### 3.6 收藏模块 `/api/v1/favorites`

| 方法 | 路径 | 说明 | 认证 | 请求体 |
|------|------|------|------|--------|
| GET | `/` | 获取收藏列表 | 登录用户 | - |
| POST | `/` | 添加收藏 | 登录用户 | AddFavoriteRequest |
| DELETE | `/{productId}` | 取消收藏 | 登录用户 | - |

### 3.7 分类模块 `/api/v1/categories`

| 方法 | 路径 | 说明 | 认证 | 参数 |
|------|------|------|------|------|
| GET | `/` | 获取分类列表 | 否 | parentId (可选) |
| POST | `/` | 创建分类 | MERCHANT | CreateCategoryRequest |

### 3.8 商家模块 `/api/v1/merchants`

| 方法 | 路径 | 说明 | 认证 | 参数 |
|------|------|------|------|------|
| GET | `/{id}` | 获取商家信息 | 否 | - |

---

## 四、CLI 命令文档

### 4.1 买家 CLI (buyer)

**编译:**
```bash
cd buyer
go build -o buyer.exe .
```

**通用参数:**
- `--server` - 后端地址 (默认: http://localhost:8080)
- `--token-file` - Token 文件路径
- `--lang` - 语言 (en/zh, 默认: en)

**命令列表:**

| 命令 | 子命令 | 说明 |
|------|--------|------|
| `login` | - | 登录 |
| `register` | - | 注册 |
| `product search` | - | 搜索商品 |
| `product view` | - | 查看商品详情 |
| `category list` | - | 浏览分类 |
| `address add` | - | 添加地址 |
| `address list` | - | 地址列表 |
| `address set-default` | - | 设置默认地址 |
| `address delete` | - | 删除地址 |
| `favorite add` | - | 添加收藏 |
| `favorite list` | - | 收藏列表 |
| `favorite remove` | - | 取消收藏 |
| `cart add` | - | 添加到购物车 |
| `cart list` | - | 购物车列表 |
| `cart update` | - | 更新数量 |
| `cart remove` | - | 移除商品 |
| `cart clear` | - | 清空购物车 |
| `order create` | - | 创建订单 |
| `order list` | - | 订单列表 |
| `order view` | - | 订单详情 |
| `review add` | - | 添加评价 |

**示例:**
```bash
# 登录
./buyer.exe login --email buyer@test.com --password 123456 --lang=zh

# 搜索商品
./buyer.exe --lang=zh product search --keyword iPhone

# 添加收藏
./buyer.exe --lang=zh favorite add --product-id 1

# 创建订单
./buyer.exe --lang=zh order create --address-id 1 --items '[{"productId":1,"quantity":2}]'
```

### 4.2 商家 CLI (merchant)

**编译:**
```bash
cd merchant
go build -o merchant.exe .
```

**命令列表:**

| 命令 | 子命令 | 说明 |
|------|--------|------|
| `login` | - | 登录 |
| `register` | - | 注册 |
| `product create` | - | 创建商品 |
| `product list` | - | 商品列表 |
| `product update` | - | 更新商品 |
| `product delete` | - | 删除商品 |
| `product upload-image` | - | 上传商品图片 |
| `category create` | - | 创建分类 |
| `order list` | - | 订单列表 |
| `order view` | - | 订单详情 |
| `order ship` | - | 订单发货 |
| `review list` | - | 评价列表 |

**示例:**
```bash
# 登录
./merchant.exe --lang=zh login --email merchant@test.com --password 123456

# 创建商品
./merchant.exe --lang=zh product create --name "iPhone 15" --price 999.99 --stock 100

# 上传商品图片
./merchant.exe --lang=zh product upload-image --id 1 --file=/path/to/image.jpg

# 订单发货
./merchant.exe --lang=zh order ship --id 1 --express "SF" --tracking "SF123456789"
```

---

## 五、错误码说明

### 5.1 HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 5.2 业务错误码

| 错误码 | 消息 Key | 说明 |
|--------|----------|------|
| 400 | err.bad_request | 请求参数错误 |
| 401 | err.unauthorized | 请先登录 |
| 403 | err.forbidden | 无权访问 |
| 404 | err.not_found | 资源不存在 |
| 500 | err.internal | 服务器内部错误 |
| 1001 | err.email_exists | 该邮箱已被注册 |
| 1002 | err.invalid_credentials | 邮箱或密码错误 |
| 1003 | err.invalid_refresh_token | 无效的刷新令牌 |
| 1004 | err.token_expired | 令牌已过期 |
| 1010 | err.user_not_found | 用户不存在 |
| 1011 | err.product_not_found | 商品不存在 |
| 1012 | err.merchant_not_found | 商家不存在 |
| 1013 | err.access_denied | 无权访问 |
| 1014 | err.insufficient_stock | 库存不足 |
| 1015 | err.cart_item_not_found | 购物车项不存在 |
| 1016 | err.address_not_found | 地址不存在 |
| 1017 | err.order_not_found | 订单不存在 |
| 1018 | err.variant_not_found | 商品规格不存在 |
| 1019 | err.order_item_not_found | 订单项不存在 |
| 1020 | err.sku_exists | SKU已存在 |
| 1021 | err.already_favorited | 已收藏过该商品 |
| 1022 | err.review_not_found | 评价不存在 |
| 1023 | err.category_not_found | 分类不存在 |
| 1024 | err.invalid_status | 状态无效 |

---

## 六、测试账号

| 角色 | 邮箱 | 密码 |
|------|------|------|
| 商家 | merchant@test.com | 123456 |
| 买家 | buyer@test.com | 123456 |

---

## 七、API 响应格式

所有 API 统一响应格式:

```json
{
  "success": true,
  "code": 200,
  "message": null,
  "data": { ... }
}
```

错误响应:

```json
{
  "success": false,
  "code": 401,
  "message": "请先登录",
  "data": null
}
```
