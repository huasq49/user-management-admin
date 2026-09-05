# User Monitor · 用户授权与管理系统

自托管的账号、设备授权、卡密、预设审核与社区管理系统。包含 Spring Boot 后端、浏览器管理控制台、Android 管理端及审核端。

> 当前定位：开源预览版，不是已完成独立安全审计的生产发行版。只用于有明确告知及合法授权的设备/账号管理，不应隐蔽采集个人数据。发布前请完成 [发布清单](docs/RELEASE_CHECKLIST.md)。

## 功能与结构

| 目录 | 内容 |
| --- | --- |
| `backend/` | **唯一维护后端**，Java 17 / Spring Boot 3 / MySQL；静态控制台及 API 文档随 JAR 发布 |
| `admin-android/` | Kotlin / Compose 管理应用 |
| `reviewer-android/` | Kotlin / Compose 预设审核应用 |
| `deploy/` | Docker Compose、HTTPS Nginx 示例 |
| `scripts/` | 随机配置生成、API 文档生成、发布检查与源码导出 |
| `docs/` | 部署、迁移、架构、隐私及发布说明 |

主要模块：统计、卡密、绑定设备、设备访问记录、用户、共享预设、审核员、聊天管理、头衔、公告、版本与运行配置。

## 本地快速启动

需要 Docker Engine + Compose v2、Python 3.10+。以下命令在项目根目录执行：

```sh
python3 scripts/init_env.py
# 仅首次创建空数据库：编辑 deploy/.env，将 DDL_AUTO 改为 update
cd deploy
docker compose up -d --build
```

浏览器访问 `http://127.0.0.1:8080/`，API 文档在 `/api-docs.html`，机器可读规范在 `/openapi.json`。管理员用户名默认为 `admin`，密码见本地 `deploy/.env` 的 `ADMIN_PASSWORD`（脚本不会打印）。首次建表后改回 `DDL_AUTO=validate`，重新创建 backend 容器。**已有数据库不要直接套用 update；先备份并在副本验证。**

默认只监听主机回环接口，不向公网开放 HTTP。公网部署必须配置 HTTPS 反向代理，参阅 [部署说明](docs/部署说明.md)。Android 默认拒绝明文 HTTP；本地 Android 调试也应提供可信 HTTPS 测试地址。

## 开发与验证

```sh
mvn -B -ntp -f backend/pom.xml verify
python3 scripts/generate_docs.py
node --check backend/src/main/resources/static/console.js
node --check backend/src/main/resources/static/docs.js
python3 scripts/release_check.py
```

Android 使用 JDK 17、Gradle 8.9、Android SDK 35：

```sh
bash deploy/build-android.sh admin-android
bash deploy/build-android.sh reviewer-android
```

未发布签名密钥；debug APK 不等于正式发行 APK。两个 Android applicationId 已统一为 `com.usermonitor.admin` / `com.usermonitor.reviewer`，不能原位覆盖旧私有包。示例服务器是 `https://api.example.com`，请在登录页修改。

## 安全与兼容性

- 没有可直接使用的默认密码；部署变量缺失或明显示例凭据会拒绝启动。
- `/api/overview` 与 `/api/dashboard` 现在也要求管理员 Bearer 令牌。
- 用户改密后撤销全部会话；授予头衔不再返回完整用户实体。
- 未鉴权 WebSocket 已关闭，聊天客户端请使用鉴权 REST 轮询。
- IP 地理数据库未随源码分发，归属地默认“未知”。
- HMAC v1 保留兼容，**不覆盖请求体和查询参数**，不是 TLS 的替代品；nonce 缓存只支持单进程，重启会丢失。不支持多副本安全防重放。
- 前端目前是本地分页；大量数据需要后续后端分页。依赖仍沿用原项目版本，发布前必须完成漏洞扫描和兼容升级。

详见 [安全策略](SECURITY.md)、[迁移说明](docs/MIGRATION.md)、[验证记录](docs/VALIDATION.md)。

## 导出开源源码

```sh
python3 scripts/export_source.py
```

得到 `dist/user-monitor-source.zip` 与 SHA-256 文件。导出采用白名单并排除备份、历史、密钥、日志、APK、JAR、数据库及构建缓存。**不要直接压缩整个工作目录**；`.local/` 和 `.backup/` 可能保留旧部署信息。导出脚本不能替代人工安全审查及 Git 历史扫描。

## 贡献与许可

见 [CONTRIBUTING.md](CONTRIBUTING.md)。新增 `LICENSE` 采用 MIT，适用于权利人有权授权的本项目原创代码；正式发布前须确认全部代码贡献、图标及第三方资源的授权，详见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
