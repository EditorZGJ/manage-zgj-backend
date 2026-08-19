# 后端变更说明（前端需适配）

## 变更零：所有接口加了 /api 前缀

```
旧: /users              → 新: /api/users
旧: /auth/login         → 新: /api/auth/login
旧: /users/export       → 新: /api/users/export
```

## 变更一：新增登录功能

所有 `/users/**` 接口现在需要**在请求头中携带 token**，否则返回 401。

### 新增接口

**1. 注册**

```
POST /auth/register
Content-Type: application/json

{
    "username": "admin",
    "password": "Admin123"
}
```

**2. 登录**

```
POST /auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "Admin123"
}
```

响应：
```json
{
    "code": 200,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "username": "admin"
    }
}
```

### 前端改动

**1. 登录页：** 调用 `/auth/login`，把返回的 token 存起来（localStorage）

**2. 所有请求（除 `/auth/**` 外）：** 请求头加上 `Authorization: Bearer 你的token`

```javascript
// axios 全局配置示例
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 登录后跳转到首页
const res = await axios.post('/auth/login', { username, password });
localStorage.setItem('token', res.data.data.token);
```

**3. 401 处理：** token 过期时跳回登录页

```javascript
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
```

**4. 退出登录**

```
POST /api/auth/logout
Authorization: Bearer 你的token
```

后端会把 token 加入黑名单，退出后该 token 再访问任何接口都会返回 401。

前端清除本地 token 后跳回登录页：`localStorage.removeItem('token')`

---

## 变更二：列表接口新增字段

分页查询 `/users` 的响应中，每条用户数据增加了以下字段：

```json
{
    "id": 1,
    "name": "张三",
    "age": 25,
    "email": "zhangsan@example.com",
    "createTime": "2026-08-19T10:30:00",
    "updateTime": "2026-08-19T10:30:00"
}
```

| 新增字段 | 类型 | 说明 |
|---------|------|------|
| `createTime` | String | 创建时间，格式 `yyyy-MM-dd'T'HH:mm:ss` |
| `updateTime` | String | 最后修改时间 |

**注意：** 新增和修改接口的请求体不变，不需要传这两个字段，后端会自动填充。

---

## 变更三：新增接口文档

详见 [API.md](API.md)（已更新登录接口和列表新字段）

---

## 需要修改的页面

| 页面 | 改动 |
|------|------|
| 登录页 | 调用 `/auth/login`，存 token |
| 所有页面 | 请求头加 `Authorization: Bearer token` |
| 用户列表 | 展示 `createTime`、`updateTime` 两列 |
| 添加/编辑用户 | 请求体不变，不需要改 |

---

## 本地测试

```bash
# 1. 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}'

# 2. 登录拿 token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}'

# 3. 用 token 访问用户接口
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer 你的token"
```