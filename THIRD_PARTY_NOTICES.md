# 第三方组件与权利核验

项目 MIT 许可不改变依赖各自的许可证。以下是审查入口，不是完成的法律审计或完整传递依赖清单。

| 组件 | 用途 | 核验入口 |
| --- | --- | --- |
| Spring Boot / Spring Framework / Spring Security | Java 服务 | Maven POM 与上游 LICENSE（通常 Apache-2.0） |
| Hibernate ORM | JPA | 上游 LGPL 等许可及对应版本 NOTICE |
| MySQL Connector/J | 数据库驱动 | 上游 GPLv2 / Universal FOSS Exception 等适用条款，分发前确认 |
| Lombok | 编译期生成 | 上游 MIT |
| Kotlin / AndroidX / Compose / Ktor / Gradle | Android 构建与 UI | 各版本 LICENSE/NOTICE |
| Docker 基础镜像 / MySQL 镜像 | 容器运行时 | 各镜像许可证与 SBOM |

旧 IP 地理数据库没有可核验的来源及再分发记录，已移出发布内容，旧 ip2region 依赖随之移除。启用替代实现时请自行取得合适授权，不得直接恢复未核验二进制文件。

发布前：确认原创代码权属，核实图标/图片/品牌名称，生成 SBOM 并审查所有直接和传递依赖。请保留适用的上游版权声明。当前清单不构成兼容性保证或法律意见。
