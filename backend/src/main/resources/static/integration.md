# User Monitor 接入指南

同源 `/openapi.json` 为完整 HTTP 字段与路径参考，`/api-docs.html` 提供交互式检索（不自动调用写接口）。文档由 `scripts/generate_docs.py` 从维护入口生成并验证路由集合。

## 认证

- 管理员：POST `/api/admin/login`；后续使用 `Authorization: Bearer <token>`。旧 `/api/overview` 和 `/api/dashboard` 同样需要管理员令牌。
- 审核员：POST `/api/reviewer/login`，令牌仅用于审核接口。
- 用户：POST `/api/client/users/login`；用户改密撤销全部会话，401 时清理本地会话并重新登录。
- 签名接口：X-Timestamp 为 Unix 秒；X-Nonce 为一次性 16–128 位字母/数字/下划线/横线；X-Signature 为 HMAC-SHA256 十六进制（64字符）。

签名原文是 `timestamp + "\n" + nonce + "\n" + METHOD + "\n" + path`，path 不含域名与查询字符串；时间窗 ±300 秒。每次重试必须产生新 nonce。此兼容协议不签名请求体和查询参数，必须 HTTPS。nonce 状态仅驻留当前进程，重启/多副本的重放风险尚未消除。

## 聊天

匿名 `/ws/chat` 已停用。使用 `GET /api/client/chat/messages?after=<最后消息id>` 并附带用户令牌轮询。不要尝试直接向内部 topic 推送消息；发消息使用 POST `/api/client/chat/messages`。

## 数据与限制

- 密码哈希与头像字节不出现在用户 JSON 中；头像使用独立资源接口。
- IP 归属地默认“未知”；不是匿名化所有设备或访问记录。
- multipart 单文件最多 2MB，总请求最多 3MB；头像业务最多 200KB，预设最多 2MB。
- 400 为部分参数/业务错误，401 为鉴权失败，413 为上传过大，503 可能表示 nonce 容量已满。其他旧接口状态语义仍需统一，以具体响应为准。
- 时间字段通常为服务器本地 ISO 时间，不含偏移；部署默认 Asia/Shanghai。
- 使用虚构测试数据；不要把真实 token 放进 URL、截图、Issue 或日志。
