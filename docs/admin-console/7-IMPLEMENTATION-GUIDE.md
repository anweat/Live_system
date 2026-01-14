# 后端管理控制台 - 开发实施指南

## 开发顺序

### Phase 1: 后端服务开发 (admin-console-service)
1. 创建Spring Boot项目
2. 集成Docker Java API
3. 实现业务API聚合层
4. 实现日志查询功能
5. 实现配置管理功能
6. 编写单元测试

### Phase 2: 前端基础搭建
1. 创建Vue 3项目
2. 配置路由和状态管理
3. 实现基础布局组件
4. 实现通用组件
5. 配置Axios和API封装

### Phase 3: 业务功能实现
1. 实现主播管理页面
2. 实现观众管理页面
3. 实现财务管理页面
4. 实现数据分析页面
5. 实现模拟测试页面

### Phase 4: 运维功能实现
1. 实现Docker管理页面
2. 实现日志查询页面
3. 实现配置管理页面
4. 实现系统设置页面

### Phase 5: 测试与优化
1. 功能测试
2. 性能优化
3. UI/UX优化
4. 部署配置

---

## 快速启动指南

### 1. 启动后端服务

#### 创建服务目录
```bash
cd services
mkdir admin-console-service
cd admin-console-service
```

#### 初始化项目结构
```
admin-console-service/
├── pom.xml
├── Dockerfile
├── docker-entrypoint.sh
└── src/
    └── main/
        ├── java/com/liveroom/adminconsole/
        │   ├── AdminConsoleApplication.java
        │   ├── config/
        │   │   ├── DockerClientConfig.java
        │   │   └── WebSocketConfig.java
        │   ├── controller/
        │   │   ├── AnchorController.java
        │   │   ├── AudienceController.java
        │   │   ├── FinanceController.java
        │   │   ├── AnalysisController.java
        │   │   ├── MockController.java
        │   │   ├── DockerController.java
        │   │   ├── LogController.java
        │   │   └── ConfigController.java
        │   ├── service/
        │   │   ├── DockerService.java
        │   │   ├── LogService.java
        │   │   ├── ConfigService.java
        │   │   └── ApiGatewayService.java
        │   ├── vo/
        │   └── util/
        └── resources/
            └── application.yml
```

#### 启动命令
```bash
# 开发环境
mvn spring-boot:run

# 生产环境
mvn clean package
java -jar target/admin-console-service.jar
```

### 2. 启动前端项目

#### 创建Vue项目
```bash
cd services/admin-console-service
npm create vue@latest web
cd web
npm install
```

#### 安装依赖
```bash
npm install element-plus
npm install @element-plus/icons-vue
npm install axios
npm install pinia
npm install echarts
npm install dayjs
npm install nprogress
```

#### 启动开发服务器
```bash
npm run dev
# 访问 http://localhost:9000
```

---

## 环境配置

### 开发环境 (Development)
```yaml
# application-dev.yml
server:
  port: 8090

services:
  anchor:
    url: http://127.0.0.1:8081
  audience:
    url: http://127.0.0.1:8082
  finance:
    url: http://127.0.0.1:8083
  analysis:
    url: http://127.0.0.1:8084
  mock:
    url: http://127.0.0.1:8087

docker:
  host: npipe:////./pipe/docker_engine

logs:
  base-path: ./services

config:
  base-path: ./services
  backup-path: ./config-backups
```

### 测试环境 (Test)
```yaml
# application-test.yml
server:
  port: 8090

services:
  anchor:
    url: http://test-server:8081
  # ... 其他服务配置
```

### 生产环境 (Production)
```yaml
# application-prod.yml
server:
  port: 8090

services:
  anchor:
    url: http://prod-server:8081
  # ... 其他服务配置
```

---

## Docker部署

### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/admin-console-service.jar app.jar
COPY docker-entrypoint.sh /

RUN chmod +x /docker-entrypoint.sh

EXPOSE 8090

ENTRYPOINT ["/docker-entrypoint.sh"]
```

### docker-compose.yml
```yaml
version: '3.8'

services:
  admin-console:
    build: ./services/admin-console-service
    container_name: admin-console-service
    ports:
      - "8090:8090"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock  # Docker socket
      - ./services:/workspace/services  # 访问其他服务配置
      - ./logs:/workspace/logs  # 日志目录
    networks:
      - live-system-network
    depends_on:
      - mysql
      - redis

networks:
  live-system-network:
    driver: bridge
```

---

## API测试

### 使用Postman测试
1. 导入API文档
2. 配置环境变量
3. 测试各个接口

### 测试用例
```
# 主播管理
GET    /api/v1/anchor/list
POST   /api/v1/anchor
GET    /api/v1/anchor/1

# Docker管理
GET    /api/v1/docker/containers
POST   /api/v1/docker/container/anchor-service/start

# 日志查询
GET    /api/v1/logs/docker/anchor-service
WS     ws://localhost:8090/ws/logs/docker/anchor-service

# 配置管理
GET    /api/v1/config/anchor-service?profile=dev
PUT    /api/v1/config/anchor-service?profile=dev
```

---

## 常见问题

### 1. Docker连接失败
**问题**: Cannot connect to Docker daemon
**解决**: 
- Windows: 确保Docker Desktop运行
- Linux: 检查docker.sock权限

### 2. 跨域问题
**问题**: CORS policy error
**解决**: 配置CORS允许前端域名
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:9000")
            .allowedMethods("*");
    }
}
```

### 3. WebSocket连接失败
**问题**: WebSocket connection failed
**解决**: 检查WebSocket配置和防火墙

---

## 性能优化建议

1. **API缓存**: 对不常变化的数据使用Redis缓存
2. **异步处理**: Docker操作使用异步执行
3. **分页查询**: 大数据量使用分页
4. **连接池**: HTTP连接使用连接池
5. **前端优化**: 
   - 路由懒加载
   - 图片压缩
   - 代码分割

---

## 安全建议

1. **认证授权**: 实现JWT认证
2. **权限控制**: RBAC权限模型
3. **操作审计**: 记录所有操作日志
4. **敏感信息**: 加密存储密码
5. **接口限流**: 防止API滥用

---

## 监控与日志

### 日志配置
```yaml
logging:
  level:
    root: INFO
    com.liveroom.adminconsole: DEBUG
  file:
    name: logs/admin-console.log
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n'
```

### 监控指标
- API响应时间
- Docker操作成功率
- WebSocket连接数
- 系统资源使用

---

## 文档维护

1. 及时更新API文档
2. 记录版本变更
3. 维护故障排查文档
4. 编写用户使用手册

---

## 相关资源

- [Docker Java API文档](https://github.com/docker-java/docker-java)
- [Element Plus文档](https://element-plus.org/)
- [Vue 3文档](https://vuejs.org/)
- [Spring Boot文档](https://spring.io/projects/spring-boot)
