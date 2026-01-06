# Docker 镜像加速配置指南

## 问题说明

如果遇到以下错误：
```
ERROR [internal] load metadata for docker.io/library/...
```

这通常是因为：
1. Docker Hub 访问速度慢或被限制
2. 网络连接问题
3. Docker 配置需要优化

## 解决方案

### 方案一：配置 Docker 镜像加速器（推荐）

#### Windows Docker Desktop

1. 右键点击系统托盘的 Docker 图标
2. 选择 "Settings" (设置)
3. 进入 "Docker Engine"
4. 在配置文件中添加以下内容：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false
}
```

5. 点击 "Apply & Restart" 重启 Docker

#### Linux 系统

编辑或创建 `/etc/docker/daemon.json`：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 方案二：使用代理（可选）

如果有代理服务器，在 Docker Desktop 设置中配置：

1. Settings → Resources → Proxies
2. 启用 Manual proxy configuration
3. 填入代理地址

### 方案三：手动拉取镜像

在运行管理脚本之前，先手动拉取所需镜像：

```powershell
# 拉取基础镜像
docker pull eclipse-temurin:11-jre-jammy
docker pull node:18-alpine
docker pull redis:7-alpine
docker pull nginx:alpine
docker pull mysql:8.0
```

## 镜像更新说明

本项目已将所有 Dockerfile 更新为使用最新的稳定镜像：

| 旧镜像 | 新镜像 | 说明 |
|--------|--------|------|
| `openjdk:11-jre-slim` | `eclipse-temurin:11-jre-jammy` | Eclipse Temurin 是 OpenJDK 的官方维护版本 |
| `node:16-alpine` | `node:18-alpine` | Node.js 18 LTS 版本 |

## 验证配置

配置完成后，验证镜像加速是否生效：

```powershell
# 查看 Docker 配置
docker info

# 测试拉取镜像速度
docker pull eclipse-temurin:11-jre-jammy
```

## 其他建议

### 清理 Docker 缓存

如果仍有问题，尝试清理 Docker 缓存：

```powershell
# 清理构建缓存
docker builder prune -a -f

# 清理所有未使用的镜像
docker image prune -a -f

# 清理所有未使用的资源
docker system prune -a -f
```

### 网络诊断

检查 Docker Hub 连接：

```powershell
# 测试连接
curl -I https://hub.docker.com

# 测试 DNS 解析
nslookup hub.docker.com
```

## 常见错误及解决

### 错误 1: `dial tcp: lookup hub.docker.com: no such host`

**原因**: DNS 解析问题

**解决**: 
- 更换 DNS 服务器（如 8.8.8.8, 114.114.114.114）
- 检查网络连接

### 错误 2: `TLS handshake timeout`

**原因**: 网络连接超时

**解决**:
- 配置镜像加速器
- 使用代理
- 检查防火墙设置

### 错误 3: `manifest unknown`

**原因**: 镜像标签不存在或输入错误

**解决**:
- 检查 Dockerfile 中的镜像标签
- 访问 Docker Hub 确认镜像是否存在

## 推荐镜像源（中国大陆）

| 提供商 | 镜像地址 | 说明 |
|--------|---------|------|
| 中科大 | https://docker.mirrors.ustc.edu.cn | 稳定，推荐 |
| 网易 | https://hub-mirror.c.163.com | 速度快 |
| 百度云 | https://mirror.baidubce.com | 新增，待测试 |
| 阿里云 | https://[您的ID].mirror.aliyuncs.com | 需注册获取专属地址 |

## 获取阿里云镜像加速地址

1. 访问 https://cr.console.aliyun.com/
2. 登录阿里云账号
3. 左侧菜单选择 "镜像加速器"
4. 复制您的专属加速地址

## 参考资料

- [Eclipse Temurin](https://adoptium.net/)
- [Docker Hub](https://hub.docker.com/)
- [Docker 官方文档](https://docs.docker.com/)
