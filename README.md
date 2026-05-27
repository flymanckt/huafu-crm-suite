# 华孚时尚 CRM 统一工程

这个目录把 CRM 后端和前端收拢成一个完整工程，后续开发、构建、部署都以 `huafu-crm-suite` 为基线。

```text
huafu-crm-suite/
  backend/      后端 Maven 多模块工程
  frontend/     前端 Vue/Vite 工程
  deploy/       nginx、systemd 等部署模板
  scripts/      构建、部署、启动、停止脚本
  logs/         本地运行日志
  docs/         项目说明和开发规范
```

完整架构说明、模块优化路线和编码规范请先阅读：

- [项目说明、架构规范与优化路线](docs/PROJECT_GUIDE.md)

## 关键能力

- 客户主数据、联系人、地址、SAP 信息、SAP 组织、画像、附件等客户核心能力。
- 线索、商机、丢单、报价、目标、拜访、日报、勤力度等销售过程能力。
- 自定义列、自定义筛选、个人变式、批量选择、批量修改等列表通用能力。
- AI 服务配置、AI 日报解析、主数据匹配和结构化写入基础能力。
- 集成平台：支持通用连接配置、SAP RFC 配置、SAP/通用接口定义、结构化字段映射、平台日志和异常数据重新推送。
- 企业微信：支持群机器人 Webhook 推送、智能机器人官方 WebSocket 长连接接收、消息日志、关键词触发日报写入和 AI 日报解析链路。

集成平台当前支持的连接/接口类型包括：

- SAP：`SAP_RFC`、`SAP_ODATA`、`SAP_IDOC`
- HTTP：`REST`、`SOAP`、`WEBHOOK`
- 文件：`SFTP`、`FTP`
- 数据和消息：`DATABASE`、`KAFKA`、`RABBITMQ`
- 扩展：`CUSTOM`

集成平台字段映射已经升级为参数结构配置：

- 接口定义按 `SAP接口` 和 `通用接口` 分流填写，SAP 维护 RFC/BAPI、OData、IDoc 相关信息，通用接口维护 HTTP 方法、内容类型和接口路径。
- 字段映射支持 `单值参数` 和 `表参数`，表参数用于 SAP `TABLES`、IDoc 明细、JSON 数组等场景。
- 映射可配置参数组/表名、映射方向、CRM 模块、CRM 字段、接口字段、字段类型、必填、默认值和转换规则。
- CRM 字段来源覆盖客户主数据和现有通用业务模块，例如产品档案、订单、发货、样品、展会、日报等。
- 客户详情的 SAP 信息保存会自动生成 `SAP_CUS` 待推送日志；SAP 编号允许先为空，可由 SAP 回传接口回填。
- 集成平台会自动消费 `PENDING/RETRYING` 日志，HTTP 类接口可直接推送；SAP RFC 已接入独立的 SAP JCo 服务，运行时缺少 JCo 库时日志会明确失败并提示处理方式。
- 平台日志会记录每次推送的字段映射取值明细，并保留接口返回原文；接口定义可按 HTTP 状态、返回文本、JSON/XML 字段或 SAP RETURN 消息判断成功失败，SAP RETURN 支持结构和表返回。

## SAP JCo 运行库

SAP RFC 执行依赖 SAP 官方 JCo 运行库。仓库不会提交 `sapjco3.jar` 和本机库，需要在部署环境补充：

```bash
cp sapjco3.jar backend/lib/
cp libsapjco3.so backend/lib/
SERVICES=customer scripts/start-services.sh
```

如 JCo 文件不放在 `backend/lib`，可通过环境变量指定：

```bash
SAP_JCO_LIB_DIR=/opt/sapjco SAP_JCO_NATIVE_DIR=/opt/sapjco SERVICES=customer scripts/start-services.sh
```

启动脚本检测到 `sapjco3.jar` 后，会自动为 `huafu-crm-customer` 加载外部 classpath 和 `java.library.path`。

## 快速构建

```bash
cd /home/kent/.hermes/profiles/dev/workspace/huafu-crm-suite
./scripts/build-all.sh
```

构建内容：

- 后端：执行 Maven 多模块打包，默认跳过测试。
- 前端：检查依赖后执行 `npm run build`。

如需执行后端测试：

```bash
SKIP_TESTS=0 ./scripts/build-all.sh
```

## 小白一键部署到服务器

如果要通过堡垒机部署到一台 Ubuntu 服务器，直接使用小白部署脚本：

```bash
cd /home/kent/.hermes/profiles/dev/workspace/huafu-crm-suite
chmod +x scripts/simple-deploy.sh scripts/simple-server-install.sh
bash scripts/simple-deploy.sh
```

脚本会询问堡垒机地址、CRM 服务器地址、部署目录和访问域名/IP，然后自动完成构建、上传、安装 Java/Nginx/PostgreSQL、初始化数据库、配置 systemd、发布前端和启动后端。

详细说明见：[华孚 CRM 小白部署说明](docs/simple_deploy.md)。

如果你想手动上传项目到服务器，不走脚本上传，也可以在服务器项目目录执行：

```bash
sudo bash scripts/simple-server-deploy.sh
```

## 本机部署

```bash
cd /home/kent/.hermes/profiles/dev/workspace/huafu-crm-suite
./scripts/deploy-local.sh
```

默认动作：

- 构建前后端。
- 将前端 `dist` 发布到 `/var/www/huafu-crm`。
- 重启后端服务：gateway、customer、opportunity、performance、target、ai、wecom。
- 日志写入 `logs/*.log`，PID 写入 `logs/pids/*.pid`。

只部署指定服务：

```bash
SERVICES="gateway customer" ./scripts/deploy-local.sh
```

跳过构建直接重启：

```bash
SKIP_BUILD=1 ./scripts/deploy-local.sh
```

停止服务：

```bash
./scripts/stop-services.sh
```

## 端口约定

- gateway: 8080
- customer: 8081
- opportunity: 8082
- performance: 8083
- target: 8084
- ai: 8085
- wecom: 8086

前端默认通过 `/api` 访问 gateway，nginx 参考配置见 `deploy/nginx/huafu-crm.conf`。

## 企业微信配置

系统管理的外围系统配置中提供企微快速配置：

- 群机器人发送：维护群机器人 Webhook、robot key、默认 @ 人、默认消息类型和默认内容。该模式只用于 CRM 主动向企微群推送消息，不具备读取群聊或接收 @ 内容的能力。
- 群机器人消息体：集成平台企微协议支持直接发送官方 `msgtype` 消息体；默认构造支持 `text`、`markdown`、`image`、`file`、`news`，并按每个机器人每分钟 20 条作为频率设计参考。
- 群消息采集写入 CRM：采用企业微信智能机器人官方 WebSocket 长连接模式。CRM 的 `huafu-crm-wecom` 服务启动后连接 `wss://openws.work.weixin.qq.com`，发送 `aibot_subscribe` 订阅，收到 `aibot_msg_callback` 后写入 CRM。
- 如果只有 Bot ID 和 Secret，在外围系统配置里填写“Bot ID”和“Bot Secret”即可；接收模式保持“官方智能机器人长连接”，长连接地址保持默认。
- CRM 侧配置项包括接收模式、长连接地址、写入模式、关键词、Bot ID、Bot Secret。
- 收到的官方消息体会落 `crm_wecom_message_log`，命中写入策略后生成日报并触发 AI 解析。兼容字段包括 `body.msgid`、`body.chatid`、`body.from.userid`、`body.msgtype`、`body.text.content`。
- 如果使用企业微信应用回调，当前已支持明文 XML、明文 JSON 和已解密内容接收；生产加密 XML 需要在接入层完成签名校验和 AES 解密，或继续扩展 wecom 服务的解密能力。

备用诊断 wecom-cli 消息能力：

```bash
./scripts/wecom-cli-diagnose.sh
```

如果输出“未获得 msg 消息能力”或 `wecom-cli msg --help` 报“暂不支持授权机器人「消息」使用权限”，需要在企业微信后台给该 Bot 开通/授权消息能力。只有 Bot ID 和 Secret 但没有 `msg` 能力时，CRM 无法读取群消息。

## 开发约定

1. 后续只在 `huafu-crm-suite` 下开发，旧目录只作备份参考。
2. 数据库结构变更必须新增 Flyway migration。
3. 后端遵循 `Controller -> Service -> Mapper -> Entity`。
4. 前端列表默认接入自定义列、自定义筛选、个人变式、批量修改。
5. 字典字段统一使用字典管理，不在页面中硬编码选项。
6. 客户详情等保存功能必须验证“保存后重新查询仍有值”。
7. 外部系统对接必须优先进入集成平台，统一维护连接配置、接口定义、结构化字段映射、日志和重推。
