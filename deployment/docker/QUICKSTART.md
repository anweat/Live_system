# 🚀 快速启动指南

## Windows 用户

### 方式一：双击运行（推荐）
1. 打开文件资源管理器
2. 导航到 `deployment` 目录
3. 双击 `manage.bat`
4. 选择 `1` 编译项目
5. 选择 `4` 一键启动所有服务

### 方式二：命令行运行
```batch
cd d:\codeproject\JavaEE\Live_system\deployment
manage.bat
```

## Linux/Mac 用户

```bash
cd deployment
chmod +x manage.sh
./manage.sh
```

## 首次使用流程

```
1. 启动 Docker Desktop (Windows) 或 Docker 服务 (Linux)
2. 运行管理脚本
3. 选择 [1] 编译项目
4. 选择 [4] 一键启动所有服务
5. 选择 [12] 检查服务状态
6. 访问 http://localhost
```

## 服务访问地址

- **Nginx 网关**: http://localhost
- **主播服务**: http://localhost:8081
- **观众服务**: http://localhost:8082
- **财务服务**: http://localhost:8083
- **Redis服务**: http://localhost:8085
- **模拟测试服务**: http://localhost:8090
- **MySQL**: localhost:3306 (用户名: root, 密码: root)
- **Redis**: localhost:6379

### 独立服务
- **DB Service**: http://localhost:8084 (使用独立配置，通过菜单 [13] 管理)

## 常用操作

### 查看日志
```
选择 [11] -> [4] -> 输入服务名称
```

### 重启服务
```
选择 [8] -> 选择重启范围
```

### 重新部署
```
选择 [9] 重新构建并重启
```

## 问题？

查看完整文档：[README.md](README.md)
