# Huafu Fashion CRM - Phase 0 Skeleton

本仓库是《华孚时尚 CRM 系统开发说明书》Phase 0 交付：Spring Boot 3 / Java 17 / Maven 多模块项目骨架，并在第二轮接入 Docker PostgreSQL、Flyway 真实迁移与 Customer 主链路集成测试。

## 模块

- `huafu-crm-common`: 通用返回、分页、异常、租户/用户上下文、Jackson、MyBatis-Plus 分页与租户插件配置。
- `huafu-crm-gateway`: Spring Cloud Gateway 入口样例，路由到 customer/wecom/ai。
- `huafu-crm-customer`: 客户价值入口，含 entity、DTO、VO、Query、Mapper、Service、Controller；接口覆盖 create/update/get/page/delete/public-pool/claim。
- `huafu-crm-wecom`: 企业微信回调入口、消息入库、日报入库、AI Mock 解析链路、业务表写入与确认推送 outbox。POST 回调通过 `MessageDispatcher` 边界处理后返回 `success`，满足 Phase 0 的 5 秒内返回设计目标。
- `huafu-crm-ai`: AI 解析测试入口、consumer 占位、`AiClient` 接口、`MockMiniMaxAiClient`。Phase 0 不强绑定未确认的 Spring AI MiniMax starter；AI 模块作为 wecom 依赖时保留普通 jar，executable jar 使用 `exec` classifier。

后续模块（合同、报价、订单、权限、报表等）将在接口边界和数据模型稳定后继续扩展。

## 数据库与本地依赖

- 开发依赖：`docker-compose-dev.yml`，包含 PostgreSQL、Redis、Nacos；RocketMQ 仍以注释形式保留，待消息契约确定后启用。
- PostgreSQL 服务：`postgres:16-alpine`，数据库 `huafu_crm`，用户 `huafu`，本地开发密码 `huafu_dev_only`，端口 `5432`。
- PostgreSQL 已配置 healthcheck：`pg_isready -U huafu -d huafu_crm`。
- 标准 Flyway migration：`huafu-crm-customer/src/main/resources/db/migration/V1__init_schema.sql`。
- 根目录脚本：`scripts/sql/V1__init_schema.sql` 保留为人工审查/同步来源；Customer 启动时加载 classpath migration。
- Customer `application.yml` 默认启用 Flyway：`spring.flyway.enabled=true`、`locations=classpath:db/migration`、`baseline-on-migrate=true`。若已有旧本地库手工初始化过 schema，baseline 可避免启动失败；新库会正常执行 V1。
- `crm_customer.id` 使用 PostgreSQL `BIGSERIAL PRIMARY KEY`，同时允许 MyBatis-Plus `@TableId(type = IdType.ASSIGN_ID)` 外部写入雪花 ID，不依赖数据库序列生成。

## Docker 权限说明

当前 WSL shell 用户组可能未刷新，直接执行 `docker ps` 可能报 permission denied。不要使用 `sudo`；使用以下方式之一：

```bash
sg docker -c 'docker ps'
sg docker -c 'docker compose -f docker-compose-dev.yml up -d postgres'
sg docker -c 'docker compose -f docker-compose-dev.yml down'
```

`scripts/dev_round2_verify.sh` 会自动检测当前 shell 是否可直接访问 Docker；如不可访问会自动用 `sg docker -c` 包装 Docker/Compose 命令。因此可直接运行：

```bash
bash scripts/dev_round2_verify.sh
```

也可显式在 docker 组上下文中运行：

```bash
sg docker -c 'bash scripts/dev_round2_verify.sh'
```

## 常用启动/停止命令

```bash
# 仅启动 PostgreSQL（集成测试需要）
sg docker -c 'docker compose -f docker-compose-dev.yml up -d postgres'

# 启动全部当前开发依赖（postgres/redis/nacos）
sg docker -c 'docker compose -f docker-compose-dev.yml up -d'

# 查看状态
sg docker -c 'docker compose -f docker-compose-dev.yml ps'

# 停止但保留 volume
sg docker -c 'docker compose -f docker-compose-dev.yml down'
```

## 验证命令

第二轮一键验证：

```bash
bash scripts/dev_round2_verify.sh
```

第三轮一键验证（企微回调 + AI Mock 端到端 + PostgreSQL）：

```bash
bash scripts/dev_round3_verify.sh
```

第三轮脚本执行内容：

- 检查 Java / Maven / Docker / Docker Compose。
- 启动 PostgreSQL 并确认 health 状态。
- 运行 `scripts/verify_structure.py`。
- 运行 `mvn -q clean verify -DskipTests=false`。
- 显式运行 `CustomerServicePostgresIT` 与 `WeComEndToEndPostgresIT`。
- 查询 `flyway_schema_history`，确认 V1/V2 migration 成功。
- 汇总 Surefire XML 测试总数。

WeCom PostgreSQL 端到端测试覆盖：

- POST `/wecom/receive` 返回 `success`，且响应时间小于 2 秒。
- `crm_wecom_message_log` 写入原始企微 XML。
- `crm_daily_report` 写入日报文本。
- `crm_ai_parse_log` 写入 Mock AI 请求/响应记录。
- `crm_lead` / `crm_opportunity` / `crm_lost_order` 根据日报中的“商情/商机/丢单”写入业务行。
- `crm_daily_report.parsed_json`、计数字段、`ai_parse_status=PARSED` 更新成功。
- `crm_wecom_push_outbox` 写入 Phase 0 Mock 确认推送记录。
- 测试使用唯一 token，并在前后清理自身数据，不污染数据库。

等价拆分命令：

```bash
python3 scripts/verify_structure.py
mvn -q clean verify -DskipTests=false
sg docker -c 'docker compose -f docker-compose-dev.yml up -d postgres'
mvn -q -pl huafu-crm-customer -am -Dtest=CustomerServicePostgresIT -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test
```

Customer PostgreSQL 集成测试覆盖：

- Flyway 已创建 `crm_customer` 表与 `flyway_schema_history`。
- `CustomerService.create` 后 `get` 可查到。
- `page` 可按唯一测试 token 查到数据。
- `moveToPublicPool` / `claim` 更新后可查。
- `delete` 为 MyBatis-Plus 逻辑删除：服务层 `get` 不可查，物理行 `deleted=true`。
- 测试使用唯一 code/name，并在前后清理自身插入数据，不依赖固定历史数据。

## 当前验收状态

已验证命令：

```bash
python3 scripts/verify_structure.py
mvn -q clean verify -DskipTests=false
sg docker -c 'docker compose -f docker-compose-dev.yml up -d postgres'
mvn -q -pl huafu-crm-customer -am -Dtest=CustomerServicePostgresIT -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test
```

基础自动化测试覆盖：

- `common`: `Result` 成功/失败结构、`BizException` code/message。
- `customer`: `CustomerServiceImpl` create/get/page/delete/claim/publicPool 单元行为，使用 Mockito mock `CustomerMapper`；另有真实 PostgreSQL 集成测试覆盖 Customer 主链路。
- `ai`: `MockMiniMaxAiClient` 稳定解析日报文本、`AiParseController` 返回 `Result<DailyReportAiResult>`。
- `wecom`: `WeComXmlParser` 解析 text XML、`WeComReceiveController` POST 回调快速返回 `success`；`WeComEndToEndPostgresIT` 覆盖企微 XML → message log → daily report → AI Mock parse → lead/opportunity/lost order → push outbox 端到端链路。

接口路径说明：客户模块按开发说明书约定暴露 `/customer/**`，gateway 路由也匹配 `/customer/**`。

## 安全说明

- 仓库不包含真实密钥。
- `application.yml` 和 compose 中仅使用本地开发占位口令，生产环境必须通过配置中心/密钥管理系统注入。
