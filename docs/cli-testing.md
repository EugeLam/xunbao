# CLI 功能测试文档

## 环境准备

### 启动后端

```bash
cd D:/code/xunbao/backend
JAVA_HOME="/c/Program Files/OpenJDK/jdk-17.0.13+11" mvn spring-boot:run
```

后端地址: `http://localhost:8080`

### 编译 CLI

```bash
# 商家端
cd D:/code/xunbao/merchant
go build -o merchant.exe .

# 买家端
cd D:/code/xunbao/buyer
go build -o buyer.exe .
```

---

## 商家端 (merchant) 测试

### 1. 登录

```bash
./merchant.exe login --email merchant@test.com --password 123456
```

**预期输出:**
```
Login successful!
Access token saved to C:\Users\Administrator/.xunbao/merchant/token
```

### 2. 商品管理

```bash
# 查看商品列表
./merchant.exe product list

# 创建商品
./merchant.exe product create \
  --name "Test Product" \
  --description "A test product" \
  --price 99.99 \
  --stock 100 \
  --category-id 1
```

### 3. 订单管理

```bash
# 查看订单列表
./merchant.exe order list

# 查看订单详情
./merchant.exe order view --id 7

# 发货（更新快递信息）
./merchant.exe order ship \
  --id 7 \
  --express "SF Express" \
  --tracking "SF123456789"
```

### 4. 分类管理

```bash
# 创建分类
./merchant.exe category create --name "Electronics"
```

### 5. 查看商品评价

```bash
./merchant.exe review list --product-id 1
```

---

## 买家端 (buyer) 测试

### 1. 登录

```bash
./buyer.exe login --email buyer@test.com --password 123456
```

**预期输出:**
```
Login successful!
Access token saved to C:\Users\Administrator/.xunbao/buyer/token
```

### 2. 商品浏览

```bash
# 搜索商品
./buyer.exe product search --keyword "iPhone"

# 查看商品详情
./buyer.exe product view --id 1

# 搜索过滤
./buyer.exe product search \
  --keyword "phone" \
  --min-price 500 \
  --max-price 2000 \
  --in-stock
```

### 3. 地址管理

```bash
# 查看地址列表
./buyer.exe address list

# 添加地址
./buyer.exe address add \
  --name "John Doe" \
  --phone "1234567890" \
  --province "Beijing" \
  --city "Beijing" \
  --district "Chaoyang" \
  --detail "Test Address 123"

# 设置默认地址
./buyer.exe address set-default --id 3

# 删除地址
./buyer.exe address delete --id 3
```

### 4. 收藏管理

```bash
# 添加收藏
./buyer.exe favorite add --product-id 1

# 查看收藏列表
./buyer.exe favorite list

# 取消收藏
./buyer.exe favorite remove --product-id 1
```

### 5. 购物车

```bash
# 添加到购物车
./buyer.exe cart add --product-id 1 --quantity 2

# 查看购物车
./buyer.exe cart list

# 更新数量
./buyer.exe cart update --id 1 --quantity 3

# 移除商品
./buyer.exe cart remove --id 1

# 清空购物车
./buyer.exe cart clear
```

### 6. 订单管理

```bash
# 查看订单列表
./buyer.exe order list

# 查看订单详情
./buyer.exe order view --id 7

# 创建订单
./buyer.exe order create \
  --address-id 3 \
  --items '[{"productId": 1, "quantity": 1}]'
```

### 7. 评价管理

```bash
# 添加评价
./buyer.exe review add \
  --product-id 1 \
  --order-id 1 \
  --rating 5 \
  --content "Great product!"
```

### 8. 分类浏览

```bash
# 查看分类列表
./buyer.exe category list

# 查看子分类
./buyer.exe category list --parent-id 1
```

---

## 全局参数

```bash
# 指定后端服务器地址
./merchant.exe --server http://localhost:8080 order list
./buyer.exe --server http://localhost:8080 order list

# 指定 Token 文件
./merchant.exe --token-file ~/.custom/token order list
```

---

## 已知问题

### serverURL 和 apiKey 为空的问题

**问题描述:** CLI 子命令包各自声明了空的 `serverURL` 和 `apiKey` 变量，导致命令执行失败。

**修复方案:** 创建共享配置包 `pkg/config/config.go`，所有命令引用同一个变量。

**修复版本:** 已在当前版本修复。

### 系统代理导致请求失败

**问题描述:** Go HTTP 客户端默认使用系统代理设置（`HTTP_PROXY` 环境变量）。如果系统配置了代理软件（如 Fiddler），请求会返回 403 错误。

**症状:**
```
Failed: <nil>
Request failed: Get "/api/v1/...": unsupported protocol scheme ""
```

**修复方案:** 在 `pkg/config/config.go` 中提供 `NewClient()` 函数，创建不使用代理的 HTTP 客户端。

**代码示例:**
```go
func NewClient() *http.Client {
    return &http.Client{
        Transport: &http.Transport{
            Proxy: nil,
        },
    }
}
```

### Token 文件尾部换行符

**问题描述:** 保存 token 时如果包含尾部换行符，会导致 `Authorization` header 无效。

**症状:**
```
Request failed: Post "http://localhost:8080/api/v1/...": net/http: invalid header field value for "Authorization"
```

**修复方案:** `LoadToken()` 使用 `strings.TrimSpace()` 去除尾部空白。

**修复版本:** 已在当前版本修复。

---

## 测试账号

| 角色 | 邮箱 | 密码 |
|------|------|------|
| 商家 | merchant@test.com | 123456 |
| 买家 | buyer@test.com | 123456 |
