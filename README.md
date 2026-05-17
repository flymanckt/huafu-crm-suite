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

## 开发约定

1. 后续只在 `huafu-crm-suite` 下开发，旧目录只作备份参考。
2. 数据库结构变更必须新增 Flyway migration。
3. 后端遵循 `Controller -> Service -> Mapper -> Entity`。
4. 前端列表默认接入自定义列、自定义筛选、个人变式、批量修改。
5. 字典字段统一使用字典管理，不在页面中硬编码选项。
6. 客户详情等保存功能必须验证“保存后重新查询仍有值”。

