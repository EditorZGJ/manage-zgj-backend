# 用户管理 API 接口文档

**基础 URL：** `http://localhost:8080`

**统一响应格式：**

```json
{
    "code": 200,        // 200=成功，400=参数错误，500=服务器错误
    "message": "操作成功",
    "data": null        // 具体数据，不同类型不一样
}
```

---

## 1. 分页查询用户

**GET** `/users`

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 第几页 |
| size | int | 否 | 10 | 每页条数 |

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "name": "张三",
                "age": 25,
                "email": "zhangsan@example.com"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

---

## 2. 添加用户

**POST** `/users/add`

**请求体（JSON）：**

```json
{
    "name": "张三",
    "age": 25,
    "email": "zhangsan@example.com"
}
```

**校验规则：** 邮箱不能重复

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": "success"
}
```

---

## 3. 修改用户

**PUT** `/users/update`

**请求体（JSON）：**

```json
{
    "id": 1,
    "name": "张三",
    "age": 30,
    "email": "zhangsan_new@example.com"
}
```

**校验规则：**
- id 必须存在
- 邮箱不能重复（排除自身）

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": "success"
}
```

---

## 4. 删除用户（逻辑删除）

**DELETE** `/users/delete/{id}`

**请求参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID，直接写在 URL 路径里 |

**注意：** 这是逻辑删除，数据仍在数据库，只是查询时过滤掉。

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": "success"
}
```

---

## 5. 恢复用户

**PUT** `/users/recover/{id}`

**请求参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID，直接写在 URL 路径里 |

**校验规则：**
- id 必须存在
- 未被删除的用户不能恢复

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": null
}
```

---

## 6. 导入用户（Excel）

**POST** `/users/import`

**请求格式：** `form-data`

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | Excel 文件（.xlsx），表头：ID、姓名、年龄、邮箱 |

**注意：** 导入是异步的，接口立即返回一个 taskId，需要通过进度接口查询导入结果。

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": "550e8400-e29b-41d4-a716-446655440000"   // taskId
}
```

---

## 7. 查询导入进度

**GET** `/users/import/progress/{taskId}`

**请求参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | String | 导入接口返回的任务 ID |

**响应示例：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 100,        // 总条数
        "processed": 45,     // 已处理
        "success": 40,       // 成功
        "failed": 5,         // 失败
        "done": false        // 是否完成
    }
}
```

**前端轮询示例：**

```javascript
const res = await axios.post('/users/import', formData);
const taskId = res.data.data;

const timer = setInterval(async () => {
    const res = await axios.get(`/users/import/progress/${taskId}`);
    const p = res.data.data;
    if (p.done) {
        clearInterval(timer);
        alert(`导入完成：成功${p.success}条，失败${p.failed}条`);
    }
}, 2000);
```

---

## 8. 导出用户（Excel）

**GET** `/users/export`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 姓名模糊搜索，不传则导出全部 |

**示例：**

```
GET /users/export               → 导出全部用户
GET /users/export?name=张       → 导出姓名含"张"的用户
```

**说明：** 直接下载 Excel 文件，文件名格式：`用户数据_yyyyMMdd_HHmmss.xlsx`

---

## 9. 下载导入模板

**GET** `/users/export/template`

**说明：** 下载一个空的 Excel 模板文件（只有表头），供填写导入数据用。

---

## 错误码说明

| code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误（如邮箱已存在、用户不存在、任务不存在） |
| 500 | 服务器内部错误（如导入失败） |