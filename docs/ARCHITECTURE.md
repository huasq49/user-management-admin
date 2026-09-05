# 架构

浏览器/Android → HTTPS Nginx → Spring MVC Controller → Service → Spring Data JPA → MySQL。

- 管理员令牌：`AdminInterceptor` 保护 `/api/admin/**`（登录除外）及两个旧统计路由。
- 客户端签名：`ClientSignatureInterceptor` 对签名路由验证时间戳、nonce、HMAC。
- 普通用户/审核员：各服务校验不同会话表；它们不是可互换的令牌。
- 静态 WebUI 与 OpenAPI 同源发布，不需要独立 Node 构建。
- 内部消息 broker 保留给现有 REST 控制器调用，但不暴露 WebSocket 端点。
- IP 归属地默认不查询。仍有设备/IP/访问时间等业务数据，须设置隐私政策。

## 后续工程化方向

拆分压缩成单行的业务类、补全 DTO/分页与统一授权、Flyway 版本化迁移、事务后事件推送、幂等卡密操作、批量查询避免 N+1、会话清理、请求体签名 v2、令牌哈希化、审计日志脱敏、SBOM 和依赖升级。不通过大规模无测试重写声称完成这些事项。
