# 华孚CRM Codex 设计规范

> 来源：Dev profile 的 Codex CLI 对整个项目重新开发后确立的设计模式
> 后续开发必须延用此思路

## 后端分层架构

### 模块结构
- `huafu-crm-common`：通用API封装（Result/PageResult/BizException）、上下文、工具类
- `huafu-crm-gateway`：Spring Cloud Gateway 网关（8080）
- `huafu-crm-customer`：客户模块（8081）
- `huafu-crm-opportunity`：商机模块（8082）
- `huafu-crm-ai`：AI模块
- `huafu-crm-performance`：勤效模块
- `huafu-crm-target`：目标管理
- `huafu-crm-wecom`：企微模块

### 标准四层
```
Controller → Service(Interface + Impl) → Mapper → Entity
```
所有模块均按此结构。

### Java Record DTO/VO模式
- **DTO用record（不可变）**：CustomerCreateDTO, CustomerUpdateDTO, CustomerQuery
- **VO用record**：CustomerVO 承载API返回数据
- **Entity用class**：对应数据库表，MyBatis-Plus实体类

### 通用API封装
- `Result<T>`：{code, message, data, timestamp}，code=200成功，!=200失败
- `PageResult<T>`：{current, size, total, records}，分页专用
- `BizException(code, message)`：业务异常，按code区分类型

### 字段设计原则（重要）
- **数据库存Integer**（type=1, status=1）
- **VO返回String**（type="ENTITY", status="POTENTIAL"）
- 用LEVEL_MAP/STATUS_MAP/BIZ_TYPE_MAP/CUST_TYPE_MAP做转换
- `toVO()` 方法中完成所有映射

示例（CustomerServiceImpl）：
```java
private static final Map<Integer, String> STATUS_MAP = Map.of(
    1, "POTENTIAL", 2, "ACTIVE", 3, "INACTIVE", 4, "LOST", 5, "NEW", 6, "KEY"
);

private CustomerVO toVO(Customer c) {
    String statusCode = c.getStatus() != null ? STATUS_MAP.getOrDefault(c.getStatus(), String.valueOf(c.getStatus())) : null;
    return new CustomerVO(c.getId(), ..., statusCode, ...);
}
```

### 安全设计：XSS防护
- `escapeHtml()`：HTML特殊字符转义（&lt; &gt; &amp; &quot; &#x27; &#x2F;）
- `sanitizeForXss()`：危险标签/属性检测，命中则抛BizException
- `safeInput()`：sanitizeForXss() → escapeHtml()，所有文本字段写入时调用

危险标签黑名单：script, iframe, object, embed, link, style, base, svg, math, body
危险属性黑名单：onerror, onload, onclick, onmouseover, onfocus, onblur, onchange, onsubmit...

### 事务
- 写操作：`@Transactional`
- 查询：`@Transactional(readOnly = true)`
- 乐观锁：`@Version`注解

### Swagger
- `@Tag(name, description)` 标注Controller
- `@Operation(summary)` 标注每个接口

### 路由设计
- `@RequestMapping("/customer")` 在Controller级别
- 前端请求 `/api/customer/**` → 网关 StripPrefix=1 → `/customer/**`
- 字典接口：`/crm/v1/dicts/codes/{dictCode}`、`/crm/v1/dicts/batch-codes`

## 前端架构（Vue3 + Element Plus）

### API层
- `request.js`：axios封装，BigInt保护（16位+数字保持字符串），401自动跳转登录，code!=200弹错
- 模块级API：`customer.js`, `admin.js`, `opportunity.js` 等

### 字典体系
- `useDict.js`：全局dictCache（reactive单例），批量加载，getDictLabel/getDictColor
- `DictTag.vue` / `DictSelect.vue`：根据itemCode自动查标签/颜色
- API：`/api/crm/v1/dicts/codes/{code}`、`/api/crm/v1/dicts/batch-codes?codes=`

### 页面Tab导航
- `TabBar.vue`：顶部页签栏，支持关闭/右键菜单
- `tabStore.js`：Pinia store，tabs列表 + activeTab，支持openTab/closeTab/switchTab
- keep-alive：MainLayout.vue中keep-alive include已打开tab的组件

### 详情页Tab模式
- `CustomerDetail.vue`：左侧el-tabs（总览/基础信息/SAP组织/联系人/转移记录/画像/附件）
- 每个Tab对应独立组件

### 开发规范（Codex确立，延用）
1. 后端每个文本字段必须走 `safeInput()` 防XSS
2. VO必须用record，DTO用record，Entity用class
3. 写操作必须 `@Transactional`
4. 前端所有字典展示用 DictTag/DictSelect 组件
5. 16位以上ID必须保持字符串（JSON.parse BigInt保护）
6. 数据库存数字、API返回itemCode字符串

## 数据库
- PostgreSQL：huafu_crm库
- MyBatis-Plus + LambdaQueryWrapper
- Flyway迁移：V1~V11
- 逻辑删除：`@TableLogic`（deleted=1为已删）
- 租户：tenantId字段

## 部署
- 后端JAR：`~/.hermes/profiles/dev/workspace/huafu-crm/huafu-crm-*/target/`
- 前端构建：`~/.hermes/profiles/dev/workspace/huafu-crm-frontend/dist/`
- nginx root：`/var/www/huafu-crm/`
- 服务端口：gateway=8080, customer=8081, opportunity=8082
