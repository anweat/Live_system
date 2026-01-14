# 部署管理功能设计

## 功能概述

提供多种部署策略的统一管理界面，支持Docker容器化部署、Kubernetes集群部署和原生服务器部署。当前版本详细实现Docker部署策略，为K8s和原生部署预留接口。

## 部署策略概览

### 1. Docker容器化部署 ⭐ (当前实现)
**适用场景**: 开发环境、小规模部署
**特点**: 
- 快速启动和停止
- 环境隔离
- 资源限制
- 通过Java Docker API管理

### 2. Kubernetes集群部署 (预留)
**适用场景**: 生产环境、大规模集群
**特点**:
- 自动扩缩容
- 负载均衡
- 服务发现
- 健康检查和自愈
**设计要点**:
- 使用Kubernetes Java Client
- 管理Deployment、Service、Pod
- 支持Helm Charts部署
- 实时监控和日志收集

### 3. 原生服务器部署 (预留)
**适用场景**: 传统部署方式、特定环境
**特点**:
- SSH远程管理
- 直接进程控制
- 系统服务管理(systemd)
**设计要点**:
- SSH连接池管理
- 远程命令执行
- 进程监控和管理
- 服务状态检测

---

## Docker部署详细设计 (当前实现)

## 技术选型

### Docker Java API
```xml
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java</artifactId>
    <version>3.3.4</version>
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.3.4</version>
</dependency>
```

## Docker连接配置

### Windows (Docker Desktop)
```yaml
docker:
  host: npipe:////./pipe/docker_engine  # Windows命名管道
  tls-verify: false
```

### Linux
```yaml
docker:
  host: unix:///var/run/docker.sock
  tls-verify: false
```

## 统一部署API设计

### 部署策略选择
```
GET /api/v1/deployment/strategy
Response: BaseResponse<DeploymentStrategy>

POST /api/v1/deployment/strategy
Body: { strategy: 'docker' | 'k8s' | 'native' }
Response: BaseResponse<Void>
```

**DeploymentStrategy**:
```typescript
{
  current: 'docker' | 'k8s' | 'native'
  available: string[]
  configured: boolean
}
```

---

## Docker部署API接口

### 1. 容器管理

#### 1.1 获取容器列表
```
GET /api/v1/docker/containers
Query: all (boolean, 是否包含停止的容器)
Response: BaseResponse<List<ContainerInfo>>
```

**ContainerInfo**:
```typescript
{
  containerId: string
  containerName: string
  image: string
  status: string  // running, exited, created, paused
  state: string
  ports: Array<PortBinding>
  createdAt: string
  startedAt: string
}
```

#### 1.2 获取容器详情
```
GET /api/v1/docker/container/{containerId}
Response: BaseResponse<ContainerDetail>
```

**ContainerDetail**:
```typescript
{
  containerId: string
  containerName: string
  image: string
  status: string
  config: {
    env: string[]
    cmd: string[]
    workingDir: string
    exposedPorts: Record<string, any>
  }
  networkSettings: {
    ipAddress: string
    ports: PortBinding[]
  }
  mounts: Array<VolumeMount>
  logs: string  // 最近100行
}
```

#### 1.3 启动容器
```
POST /api/v1/docker/container/{containerId}/start
Response: BaseResponse<Void>
```

#### 1.4 停止容器
```
POST /api/v1/docker/container/{containerId}/stop
Query: timeout (seconds, 默认10)
Response: BaseResponse<Void>
```

#### 1.5 重启容器
```
POST /api/v1/docker/container/{containerId}/restart
Query: timeout (seconds, 默认10)
Response: BaseResponse<Void>
```

#### 1.6 删除容器
```
DELETE /api/v1/docker/container/{containerId}
Query: force (boolean, 强制删除)
Response: BaseResponse<Void>
```

---

### 2. 镜像管理

#### 2.1 获取镜像列表
```
GET /api/v1/docker/images
Response: BaseResponse<List<ImageInfo>>
```

**ImageInfo**:
```typescript
{
  imageId: string
  repoTags: string[]
  size: number
  createdAt: string
}
```

#### 2.2 拉取镜像
```
POST /api/v1/docker/image/pull
Body: { imageName: string, tag: string }
Response: BaseResponse<Void>
```

---

### 3. 服务快捷操作

#### 3.1 获取所有微服务状态
```
GET /api/v1/docker/services/status
Response: BaseResponse<List<ServiceStatus>>
```

**ServiceStatus**:
```typescript
{
  serviceName: string  // anchor-service, audience-service等
  containerId: string
  status: string
  uptime: string
  restartCount: number
  healthCheck: string  // healthy, unhealthy, none
}
```

#### 3.2 批量启动服务
```
POST /api/v1/docker/services/start
Body: { services: string[] }  // 服务名数组
Response: BaseResponse<BatchOperationResult>
```

#### 3.3 批量停止服务
```
POST /api/v1/docker/services/stop
Body: { services: string[] }
Response: BaseResponse<BatchOperationResult>
```

#### 3.4 批量重启服务
```
POST /api/v1/docker/services/restart
Body: { services: string[] }
Response: BaseResponse<BatchOperationResult>
```

**BatchOperationResult**:
```typescript
{
  successCount: number
  failureCount: number
  results: Array<{
    serviceName: string
    success: boolean
    message: string
  }>
}
```

---

### 4. Docker Compose操作

#### 4.1 启动Docker Compose
```
POST /api/v1/docker/compose/up
Body: { projectPath: string, detach: boolean }
Response: BaseResponse<Void>
```

#### 4.2 停止Docker Compose
```
POST /api/v1/docker/compose/down
Body: { projectPath: string, removeVolumes: boolean }
Response: BaseResponse<Void>
```

#### 4.3 查看Compose服务状态
```
GET /api/v1/docker/compose/ps
Query: projectPath
Response: BaseResponse<List<ComposeServiceStatus>>
```

---

---

## 部署策略接口设计

### DeploymentStrategy接口
```java
public interface DeploymentStrategy {
    // 获取服务列表
    List<ServiceInfo> listServices();
    
    // 启动服务
    void startService(String serviceName);
    
    // 停止服务
    void stopService(String serviceName);
    
    // 重启服务
    void restartService(String serviceName);
    
    // 获取服务状态
    ServiceStatus getServiceStatus(String serviceName);
    
    // 获取服务日志
    List<String> getServiceLogs(String serviceName, int tail);
}
```

### 策略实现类
```java
// Docker策略实现
public class DockerDeploymentStrategy implements DeploymentStrategy {
    // Docker具体实现
}

// K8s策略实现（预留）
public class K8sDeploymentStrategy implements DeploymentStrategy {
    // TODO: 实现K8s部署逻辑
}

// 原生策略实现（预留）
public class NativeDeploymentStrategy implements DeploymentStrategy {
    // TODO: 实现原生部署逻辑
}
```

### 策略工厂
```java
@Component
public class DeploymentStrategyFactory {
    
    @Autowired
    private DockerDeploymentStrategy dockerStrategy;
    
    public DeploymentStrategy getStrategy(String strategyType) {
        switch (strategyType.toLowerCase()) {
            case "docker":
                return dockerStrategy;
            case "k8s":
                throw new UnsupportedOperationException("K8s策略暂未实现");
            case "native":
                throw new UnsupportedOperationException("原生策略暂未实现");
            default:
                throw new IllegalArgumentException("未知的部署策略: " + strategyType);
        }
    }
}
```

---

## Docker部署核心实现

### DockerClientConfig
```java
@Configuration
public class DockerClientConfig {
    
    @Value("${docker.host:npipe:////./pipe/docker_engine}")
    private String dockerHost;
    
    @Bean
    public DockerClient dockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .build();
            
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .build();
            
        return DockerClientImpl.getInstance(config, httpClient);
    }
}
```

### DockerDeploymentStrategy实现
```java
@Service
public class DockerDeploymentStrategy implements DeploymentStrategy {
    
    @Autowired
    private DockerClient dockerClient;
    
    // 列出所有容器
    public List<Container> listContainers(boolean showAll) {
        return dockerClient.listContainersCmd()
            .withShowAll(showAll)
            .exec();
    }
    
    // 启动容器
    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }
    
    // 停止容器
    public void stopContainer(String containerId, int timeout) {
        dockerClient.stopContainerCmd(containerId)
            .withTimeout(timeout)
            .exec();
    }
    
    // 重启容器
    public void restartContainer(String containerId) {
        dockerClient.restartContainerCmd(containerId).exec();
    }
    
    // 获取容器详情
    public InspectContainerResponse inspectContainer(String containerId) {
        return dockerClient.inspectContainerCmd(containerId).exec();
    }
}
```

---

## K8s部署设计要点 (预留)

### 技术选型
```xml
<dependency>
    <groupId>io.kubernetes</groupId>
    <artifactId>client-java</artifactId>
    <version>18.0.0</version>
</dependency>
```

### 核心功能
- Deployment管理（创建、更新、删除、扩缩容）
- Service管理（ClusterIP、NodePort、LoadBalancer）
- Pod管理（查询、重启、删除）
- ConfigMap和Secret管理
- 命名空间管理
- 日志收集和监控

### API设计概要
```
GET    /api/v1/k8s/deployments
POST   /api/v1/k8s/deployment/{name}/scale
GET    /api/v1/k8s/pods
GET    /api/v1/k8s/services
```

---

## 原生部署设计要点 (预留)

### 技术选型
```xml
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>
```

### 核心功能
- SSH连接管理
- 远程命令执行（启动、停止、重启服务）
- 文件传输（上传jar包、配置文件）
- 进程监控（ps、top命令）
- Systemd服务管理
- 日志文件读取

### API设计概要
```
POST   /api/v1/native/server/connect
POST   /api/v1/native/service/{name}/start
GET    /api/v1/native/process/{pid}/status
POST   /api/v1/native/file/upload
```

### 实现要点
```java
public class NativeDeploymentStrategy implements DeploymentStrategy {
    
    // SSH会话管理
    private Map<String, Session> sshSessions;
    
    // 执行远程命令
    private String executeCommand(String host, String command) {
        // SSH连接执行命令
        // 返回执行结果
    }
    
    // 启动服务（systemd）
    public void startService(String serviceName) {
        executeCommand(host, "sudo systemctl start " + serviceName);
    }
    
    // 停止服务
    public void stopService(String serviceName) {
        executeCommand(host, "sudo systemctl stop " + serviceName);
    }
    
    // 检查服务状态
    public ServiceStatus getServiceStatus(String serviceName) {
        String result = executeCommand(host, "systemctl status " + serviceName);
        // 解析结果返回状态
    }
}
```

---

## 服务映射配置

### 预定义服务列表
```yaml
services:
  mapping:
    - name: anchor-service
      container-name: anchor-service
      port: 8081
      health-check-url: http://localhost:8081/anchor/actuator/health
    - name: audience-service
      container-name: audience-service
      port: 8082
      health-check-url: http://localhost:8082/audience/actuator/health
    - name: finance-service
      container-name: finance-service
      port: 8083
      health-check-url: http://localhost:8083/finance/actuator/health
    - name: data-analysis-service
      container-name: data-analysis-service
      port: 8084
      health-check-url: http://localhost:8084/analysis/actuator/health
    - name: redis-service
      container-name: redis-service
      port: 8085
      health-check-url: http://localhost:8085/redis/actuator/health
    - name: db-service
      container-name: db-service
      port: 8086
      health-check-url: http://localhost:8086/api/database/health
    - name: mock-service
      container-name: mock-service
      port: 8087
      health-check-url: http://localhost:8087/api/v1/health
    - name: nginx
      container-name: nginx
      port: 80
      health-check-url: http://localhost:80
    - name: mysql
      container-name: mysql
      port: 3306
    - name: redis
      container-name: shared-redis
      port: 6379
```

---

## 错误处理

### 常见错误
1. **Docker未启动**: 检测连接失败，提示启动Docker Desktop
2. **容器不存在**: 返回404错误
3. **容器已停止**: 停止已停止的容器不报错
4. **权限不足**: Windows需要管理员权限

### 异常处理
```java
@ControllerAdvice
public class DockerExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public BaseResponse<?> handleNotFound(NotFoundException e) {
        return BaseResponse.error(404, "容器或镜像不存在");
    }
    
    @ExceptionHandler(ConflictException.class)
    public BaseResponse<?> handleConflict(ConflictException e) {
        return BaseResponse.error(409, "操作冲突：" + e.getMessage());
    }
    
    @ExceptionHandler(DockerException.class)
    public BaseResponse<?> handleDockerError(DockerException e) {
        return BaseResponse.error(500, "Docker操作失败：" + e.getMessage());
    }
}
```

---

## 安全考虑

1. **权限控制**: 仅管理员可执行容器操作
2. **操作日志**: 记录所有容器操作日志
3. **危险操作确认**: 删除容器需二次确认
4. **资源限制**: 限制批量操作数量

---

## 前端交互设计

### 部署策略切换
```vue
<template>
  <div class="deployment-strategy">
    <el-radio-group v-model="strategy" @change="onStrategyChange">
      <el-radio label="docker">Docker容器化</el-radio>
      <el-radio label="k8s" disabled>Kubernetes (敬请期待)</el-radio>
      <el-radio label="native" disabled>原生部署 (敬请期待)</el-radio>
    </el-radio-group>
  </div>
</template>
```

### 统一服务管理界面
根据选择的部署策略，动态显示对应的管理界面：
- Docker: 容器列表和操作
- K8s: Deployment/Pod列表
- Native: 服务器列表和进程管理

---

### 容器列表页面
- 表格展示所有容器
- 状态图标（运行中/已停止）
- 操作按钮（启动/停止/重启/查看日志）
- 批量选择操作

### 容器详情页面
- 基本信息展示
- 环境变量
- 端口映射
- 挂载卷
- 实时日志流

### 快捷操作面板
- 一键启动所有服务
- 一键停止所有服务
- 服务健康检查状态
- 服务依赖关系图

---

## Docker部署扩展功能

### 1. 容器监控
- CPU使用率
- 内存使用率
- 网络流量
- 磁盘IO

### 2. 日志流
- WebSocket实时推送日志
- 日志过滤和搜索
- 日志导出

### 3. 资源统计
- 容器资源使用趋势图
- 资源告警
- 资源使用排行
