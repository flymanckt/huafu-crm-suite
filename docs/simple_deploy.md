# 华孚 CRM 小白部署说明

这套脚本适合一台 Ubuntu 服务器，通过堡垒机 SSH 访问。

## 你只需要准备

1. 堡垒机 SSH 地址，例如：`jumpuser@1.2.3.4`
2. CRM 服务器 SSH 地址，例如：`deploy@10.0.0.12`
3. CRM 服务器上的用户可以执行 `sudo`

## 方式一：在本机自动上传并部署

在本机 WSL 的项目目录执行：

```bash
cd /home/kent/.hermes/profiles/dev/workspace/huafu-crm-suite
chmod +x scripts/simple-deploy.sh scripts/simple-server-install.sh
bash scripts/simple-deploy.sh
```

脚本会依次询问：

- 堡垒机地址
- CRM 服务器地址
- 服务器部署目录，直接回车默认 `/opt/huafu-crm-suite`
- 访问域名/IP，没有域名直接回车默认 `_`

然后它会自动完成：

- 构建后端
- 构建前端
- 打包上传
- 服务器安装 Java、Nginx、PostgreSQL
- 初始化数据库
- 配置 Nginx
- 配置 systemd 服务
- 启动 CRM

## 方式二：你手动上传项目到服务器后部署

如果你不想让脚本自动上传，就用这个方式。

先把整个 `huafu-crm-suite` 文件夹上传到服务器，例如上传到：

```bash
/home/deploy/huafu-crm-suite
```

然后通过堡垒机登录服务器，在服务器上执行：

```bash
cd /home/deploy/huafu-crm-suite
sudo bash scripts/simple-server-deploy.sh
```

脚本会自动把项目发布到 `/opt/huafu-crm-suite`，并完成安装、构建、数据库初始化、Nginx 配置和服务启动。

如果你有域名，可以这样执行：

```bash
SERVER_NAME="crm.your-domain.com" sudo -E bash scripts/simple-server-deploy.sh
```

如果你想换部署目录：

```bash
APP_DIR="/data/huafu-crm-suite" sudo -E bash scripts/simple-server-deploy.sh
```

## 以后更新系统

自动上传方式还是执行同一个命令：

```bash
bash scripts/simple-deploy.sh
```

手动上传方式，就是重新上传整个项目文件夹，然后在服务器上执行：

```bash
sudo bash scripts/simple-server-deploy.sh
```

脚本会保留服务器上的数据库密码和 JWT 密钥，不会覆盖已有业务数据。

## 常用排查命令

登录服务器后：

```bash
sudo systemctl status huafu-crm-suite
sudo journalctl -u huafu-crm-suite -n 100
tail -n 100 /opt/huafu-crm-suite/logs/gateway.log
tail -n 100 /opt/huafu-crm-suite/logs/customer.log
```

重启：

```bash
sudo systemctl restart huafu-crm-suite
sudo systemctl reload nginx
```

## 注意

- 服务器 80 端口用于访问 CRM。
- 8080-8086 是内部服务端口，不要开放给外网。
- SSH 端口建议只允许堡垒机访问。
- 正式生产建议后续再加 HTTPS 证书。
