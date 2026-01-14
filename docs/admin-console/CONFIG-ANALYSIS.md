# 微服务配置文件分析与优化方案

## 当前配置状态分析

### 已有配置的服务
| 服务名称 | 端口 | 主配置 | 多环境配置 | 数据库 | Redis | 日志 | 监控 |
|---------|------|--------|-----------|--------|-------|------|------|
| anchor-service | 8081 | ✅ | ✅ dev/prod | ✅ MySQL | ✅ DB0 | ✅ | ✅ |
| audience-service | 8082 | ✅ | ✅ dev/prod | ✅ MySQL | ✅ DB1 | ✅ | ✅ |
| finance-service | 8083 | ✅ | ✅ dev/prod | ✅ MySQL | ✅ DB2 | ✅ | ✅ |
| data-analysis-service | 8088 | ⚠️ | ✅ dev | ❌ | ❌ | ⚠️ | ❌ |
| redis-service | 8085 | ✅ | ❌ | ❌ | ✅ DB10 | ✅ | ❌ |
| mock-service | 8090 | ✅ | ❌ | ✅ MySQL | ❌ | ❌ | ❌ |

### 发现的问题

#### 1. 端口冲突
- ❌ **finance-service**: 配置端口为8082，与audience-service冲突
- ❌ **mock-service**: 配置端口为8090，与admin-console-service冲突

#### 2. 缺失关键配置
- ❌ **data-analysis-service**: 缺少完整的数据库和Redis配置
- ❌ **redis-service**: 缺少actuator监控配置
- ❌ **mock-service**: 缺少日志配置、actuator监控配置

#### 3. 多环境配置不完整
- ⚠️ **redis-service**: 没有dev/test/prod环境配置
- ⚠️ **mock-service**: 没有dev/test/prod环境配置
- ⚠️ **data-analysis-service**: 只有dev配置

#### 4. 日志配置不统一
- 日志格式不一致
- 日志级别设置不统一
- 日志文件路径不规范

#### 5. 缺少管理控制台所需配置
- ❌ 所有服务缺少 application-test.yml (测试环境)
- ⚠️ 部分服务actuator端点暴露不完整
- ⚠️ 缺少统一的健康检查配置

---

## 优化方案

### 标准化配置结构

每个服务应包含以下配置文件：
```
src/main/resources/
├── application.yml           # 主配置（通用配置）
├── application-dev.yml       # 开发环境
├── application-test.yml      # 测试环境
└── application-prod.yml      # 生产环境
```

### 统一配置项

#### 1. 服务基础配置
```yaml
server:
  port: ${PORT}
  servlet:
    context-path: /${SERVICE_NAME}

spring:
  application:
    name: ${service-name}
  profiles:
    active: ${PROFILE:dev}
```

#### 2. 数据库配置（需要数据库的服务）
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME}
    username: ${DB_USER:root}
    password: ${DB_PASS:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

#### 3. Redis配置
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASS:}
    database: ${REDIS_DB:0}
    timeout: 2000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

#### 4. 统一日志配置
```yaml
logging:
  level:
    root: INFO
    com.liveroom: ${LOG_LEVEL:DEBUG}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/${spring.application.name}.log
    max-size: 10MB
    max-history: 30
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
      total-size-cap: 1GB
```

#### 5. Actuator监控配置（所有服务必须）
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    loggers:
      enabled: true
  health:
    defaults:
      enabled: true
```

---

## 需要修复的配置

### 1. finance-service
**问题**: 端口8082冲突
**修复**: 改为8083

### 2. mock-service  
**问题**: 端口8090冲突、缺少日志和监控配置
**修复**: 改为8087，添加完整配置

### 3. data-analysis-service
**问题**: 缺少数据库、Redis和完整监控配置
**修复**: 添加完整配置

### 4. redis-service
**问题**: 缺少监控配置和多环境配置
**修复**: 添加actuator和环境配置

---

## 服务端口分配表

| 服务 | 端口 | Context Path |
|------|------|-------------|
| anchor-service | 8081 | /anchor |
| audience-service | 8082 | /audience |
| finance-service | 8083 | /finance |
| data-analysis-service | 8084 | /analysis |
| redis-service | 8085 | /redis |
| db-service | 8086 | /api/database |
| mock-service | 8087 | /api/v1/mock |
| nginx | 80 | / |
| admin-console-service | 8090 | /admin |

---

## 环境变量配置建议

为了便于管理控制台动态修改配置，建议使用环境变量：

### 开发环境 (dev)
```bash
PROFILE=dev
DB_HOST=localhost
DB_PORT=3306
REDIS_HOST=localhost
REDIS_PORT=6379
LOG_LEVEL=DEBUG
```

### 测试环境 (test)
```bash
PROFILE=test
DB_HOST=test-mysql
DB_PORT=3306
REDIS_HOST=test-redis
REDIS_PORT=6379
LOG_LEVEL=INFO
```

### 生产环境 (prod)
```bash
PROFILE=prod
DB_HOST=prod-mysql
DB_PORT=3306
REDIS_HOST=prod-redis
REDIS_PORT=6379
LOG_LEVEL=WARN
```

---

## 下一步行动

1. ✅ 修复finance-service端口配置
2. ✅ 修复mock-service端口和配置
3. ✅ 完善data-analysis-service配置
4. ✅ 为redis-service添加监控配置
5. ✅ 为所有服务添加test环境配置
6. ✅ 统一日志配置格式
7. ✅ 验证所有服务配置一致性
