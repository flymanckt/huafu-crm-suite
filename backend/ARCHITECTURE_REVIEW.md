# 华孚 CRM 架构审视与重构路线

## 当前判断

项目已经具备微服务雏形：`gateway` 统一入口，客户、商机、勤力度、目标、企微、AI 等模块拆分为独立 Spring Boot 服务，前端以 Vue3 + Element Plus 承载业务页面。这适合 CRM 后续增长，但当前实现仍处在“功能快速补齐”阶段，核心风险是领域模型、配置能力和页面复用层没有完全收拢，继续追加需求会快速产生重复代码和保存链路不一致。

## 已落地优化

1. 统一输入安全能力  
   新增 `InputSanitizer` 到 common 层，后续服务共享 XSS 检测与文本转义逻辑，不再在每个 Service 里复制黑名单和转义方法。

2. 通用台账回归标准分层  
   `ModuleRecordController` 从直接调用 Mapper 调整为 `Controller -> Service -> Mapper`，新增 `ModuleRecordService`、`ModuleRecordSaveDTO`、`ModuleRecordVO`，让通用台账模块符合现有设计规范。

3. 前端列表配置能力收拢  
   列配置和筛选变式已按 `pageCode` 隔离，客户列表扩展为完整客户字段清单，通用台账页复用同一套列配置与个人变式能力。

4. 时间类型一致性修正  
   个人配置实体中的 `TIMESTAMPTZ` 映射改为 `OffsetDateTime`，避免 PostgreSQL 读取时报类型转换异常。

## 主要架构问题

### P0：版本库污染

`target/`、jar、class 等构建产物虽然已在 `.gitignore` 中忽略，但历史上仍被 Git 跟踪，导致每次构建产生大量无意义变更。建议单独执行一次仓库清理：从 Git 索引移除所有构建产物，只保留源码和迁移脚本。

### P0：客户领域模型过胖

`crm_customer` 承载了基础信息、财务、SAP、关系、分类、业务画像等大量字段。短期能跑，长期会导致：

- DTO/VO 构造器参数过长，新增字段容易错位。
- 主表更新接口过宽，任何页面都可能覆盖不相关字段。
- SAP 组织、伙伴、画像、客户关系的边界不清。

建议逐步拆成聚合：`CustomerBase`、`CustomerProfile`、`CustomerFinance`、`CustomerSapOrg`、`CustomerRelationship`，主表只保留身份与归属字段。

### P1：通用台账是过渡方案

`crm_module_record.payload_json` 适合快速承载展会、色卡、样品、报表等需求，但不能长期替代强业务模型。后续高频模块应从通用台账迁出为专用表和专用服务，比如色卡派送、样品库存、展会洽谈。

### P1：枚举与字典双轨

后端部分字段用 Map 转 itemCode，前端又有 `customerFields.js` 本地映射，同时系统还有字典接口。三套规则并存会产生保存/回显不一致。建议统一为“数据库存 itemCode 或数字 + 字典服务解释”，前后端都从字典源读取。

### P1：服务边界不一致

客户服务同时承载客户、系统用户、角色、部门、字典、用户配置、通用台账。短期可接受，但中期应拆出：

- `huafu-crm-system`：用户、角色、部门、字典、个人配置。
- `huafu-crm-customer`：客户主数据与客户关系。
- `huafu-crm-business-ledger`：台账类需求，后续再拆分。

### P2：前端列表页复用不足

列表页正在重复实现搜索、分页、列配置、变式、表格渲染。建议沉淀 `SmartListPage` 组件，由配置驱动搜索项、列、操作按钮和数据源。

## 推荐重构顺序

1. 清理 Git 跟踪的构建产物，建立干净基线。
2. 抽出 `huafu-crm-system` 或至少把 system 相关包名从 customer 模块迁出。
3. 把客户主数据拆成窄 DTO，按 Tab 分接口保存，避免一个大 update 覆盖所有字段。
4. 字典/枚举统一：废除前端硬编码映射，全部走字典服务。
5. 通用台账中访问频率最高的模块专用化：色卡派送、样品库存、展会洽谈优先。
6. 前端沉淀 `SmartListPage`，批量替换商机、目标、报价、勤力度列表。
7. 加 CI：后端编译+单测、前端 build、Flyway validate、关键接口 smoke test。

## 架构约束

- Controller 不直接访问 Mapper。
- 所有写接口使用 DTO，所有返回使用 VO。
- 文本输入统一走 `InputSanitizer`。
- 新建数据库结构只能通过 Flyway migration。
- 前端新增列表默认接入列配置和个人变式。
- 业务强模型优先使用专用表，通用 JSON 台账只用于探索期和低频台账。
