# 日志管理功能设计

## 功能概述

提供Docker容器日志和应用程序日志的查询、搜索、实时监控功能。

## 日志类型

### 1. Docker容器日志
- **来源**: Docker日志API
- **内容**: 容器标准输出和标准错误
- **格式**: 纯文本

### 2. 应用日志
- **来源**: 应用日志文件
- **框架**: 使用common模块的Logger
- **格式**: JSON/文本格式
- **位置**: 容器内/本地日志目录

---

## API接口设计

### 1. Docker日志

#### 1.1 获取容器日志
```
GET /api/v1/logs/docker/{containerId}
Query:
  - tail: number (最近N行，默认100)
  - since: timestamp (起始时间)
  - follow: boolean (是否实时跟踪)
  - timestamps: boolean (是否显示时间戳)
Response: BaseResponse<DockerLogsResponse>
```

**DockerLogsResponse**:
```typescript
{
  containerId: string
  containerName: string
  logs: string[]
  timestamp: string
}
```

#### 1.2 实时日志流（WebSocket）
```
WebSocket: /ws/logs/docker/{containerId}
Message: {
  timestamp: string
  stream: 'stdout' | 'stderr'
  message: string
}
```

#### 1.3 导出容器日志
```
GET /api/v1/logs/docker/{containerId}/export
Query: format (txt, json), startTime, endTime
Response: File download
```

---

### 2. 应用日志

#### 2.1 查询应用日志
```
GET /api/v1/logs/application/{serviceName}
Query:
  - level: string (DEBUG, INFO, WARN, ERROR)
  - startTime: timestamp
  - endTime: timestamp
  - keyword: string (关键词搜索)
  - page: number
  - size: number
Response: BaseResponse<PageResponse<LogEntry>>
```

**LogEntry**:
```typescript
{
  id: string
  timestamp: string
  level: string
  logger: string
  thread: string
  message: string
  exception: string
  context: Record<string, any>
}
```

#### 2.2 日志级别统计
```
GET /api/v1/logs/application/{serviceName}/stats
Query: startTime, endTime
Response: BaseResponse<LogStats>
```

**LogStats**:
```typescript
{
  serviceName: string
  totalCount: number
  levelCounts: {
    DEBUG: number
    INFO: number
    WARN: number
    ERROR: number
  }
  timeRange: {
    start: string
    end: string
  }
}
```

#### 2.3 错误日志汇总
```
GET /api/v1/logs/errors
Query: startTime, endTime, serviceName (可选)
Response: BaseResponse<List<ErrorSummary>>
```

**ErrorSummary**:
```typescript
{
  serviceName: string
  errorType: string
  errorMessage: string
  count: number
  firstOccurrence: string
  lastOccurrence: string
  stackTrace: string
}
```

#### 2.4 实时应用日志（WebSocket）
```
WebSocket: /ws/logs/application/{serviceName}
Query: level (过滤级别)
Message: LogEntry
```

---

### 3. 日志搜索

#### 3.1 全文搜索
```
GET /api/v1/logs/search
Query:
  - query: string (搜索关键词)
  - services: string[] (服务列表)
  - level: string
  - startTime: timestamp
  - endTime: timestamp
  - page: number
  - size: number
Response: BaseResponse<PageResponse<LogSearchResult>>
```

**LogSearchResult**:
```typescript
{
  serviceName: string
  logEntry: LogEntry
  highlights: string[]  // 高亮的匹配文本
  score: number  // 相关性分数
}
```

---

## 核心实现

### 1. Docker日志读取

#### DockerLogService
```java
@Service
public class DockerLogService {
    
    @Autowired
    private DockerClient dockerClient;
    
    // 获取容器日志
    public List<String> getContainerLogs(String containerId, int tail) {
        try (LogContainerResultCallback callback = new LogContainerResultCallback()) {
            List<String> logs = new ArrayList<>();
            
            dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTail(tail)
                .withTimestamps(true)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        logs.add(frame.toString());
                    }
                })
                .awaitCompletion();
                
            return logs;
        }
    }
    
    // 实时日志流
    public void followContainerLogs(String containerId, 
                                   Consumer<String> logConsumer) {
        dockerClient.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withFollowStream(true)
            .withTimestamps(true)
            .exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    logConsumer.accept(frame.toString());
                }
            });
    }
}
```

---

### 2. 应用日志读取

#### 日志文件位置
```
services/
├── anchor-service/logs/
│   ├── app.log
│   ├── error.log
│   └── access.log
├── audience-service/logs/
├── finance-service/logs/
└── ...
```

#### ApplicationLogService
```java
@Service
public class ApplicationLogService {
    
    @Value("${logs.base-path:./services}")
    private String logsBasePath;
    
    // 读取日志文件
    public PageResponse<LogEntry> queryLogs(
            String serviceName, 
            LogQueryParams params) {
        
        Path logFile = Paths.get(logsBasePath, serviceName, "logs", "app.log");
        
        List<LogEntry> entries = Files.lines(logFile)
            .map(this::parseLogLine)
            .filter(entry -> matchesQuery(entry, params))
            .skip((params.getPage() - 1) * params.getSize())
            .limit(params.getSize())
            .collect(Collectors.toList());
            
        return new PageResponse<>(entries, params.getPage(), params.getSize());
    }
    
    // 解析日志行
    private LogEntry parseLogLine(String line) {
        // 解析日志格式
        // 2026-01-07 10:00:00.123 [INFO] [main] com.example.Service - Message
        // 返回LogEntry对象
    }
}
```

---

### 3. WebSocket实时推送

#### LogWebSocketHandler
```java
@Component
public class LogWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String containerId = extractContainerId(session);
        sessions.put(session.getId(), session);
        
        // 启动日志流
        startLogStream(containerId, session);
    }
    
    private void startLogStream(String containerId, WebSocketSession session) {
        dockerLogService.followContainerLogs(containerId, log -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(log));
                }
            } catch (IOException e) {
                log.error("Failed to send log message", e);
            }
        });
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }
}
```

#### WebSocket配置
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private LogWebSocketHandler logWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(logWebSocketHandler, "/ws/logs/docker/{containerId}")
            .setAllowedOrigins("*");
            
        registry.addHandler(logWebSocketHandler, "/ws/logs/application/{serviceName}")
            .setAllowedOrigins("*");
    }
}
```

---

### 4. 日志解析与格式化

#### Common Logger格式
```java
// common模块使用的日志格式
@Slf4j
@Aspect
@Component
public class LogAspect {
    // 统一日志格式：
    // [timestamp] [level] [thread] [logger] - message [context]
}
```

#### 日志解析器
```java
public class LogParser {
    
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "\\[(.*?)\\]\\s*\\[(.*?)\\]\\s*\\[(.*?)\\]\\s*\\[(.*?)\\]\\s*-\\s*(.*)"
    );
    
    public LogEntry parse(String logLine) {
        Matcher matcher = LOG_PATTERN.matcher(logLine);
        if (matcher.matches()) {
            return LogEntry.builder()
                .timestamp(matcher.group(1))
                .level(matcher.group(2))
                .thread(matcher.group(3))
                .logger(matcher.group(4))
                .message(matcher.group(5))
                .build();
        }
        return null;
    }
}
```

---

## 前端展示

### 日志查看器组件
```vue
<template>
  <div class="log-viewer">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-select v-model="selectedService">
        <el-option label="Anchor Service" value="anchor-service" />
        <el-option label="Audience Service" value="audience-service" />
      </el-select>
      
      <el-select v-model="logLevel">
        <el-option label="All" value="" />
        <el-option label="ERROR" value="ERROR" />
        <el-option label="WARN" value="WARN" />
        <el-option label="INFO" value="INFO" />
      </el-select>
      
      <el-input v-model="searchKeyword" placeholder="搜索关键词" />
      <el-button @click="search">搜索</el-button>
      <el-button @click="toggleRealtime">实时日志</el-button>
      <el-button @click="exportLogs">导出</el-button>
    </div>
    
    <!-- 日志内容 -->
    <div class="log-content" ref="logContainer">
      <div v-for="log in logs" :key="log.id" :class="['log-line', log.level]">
        <span class="timestamp">{{ log.timestamp }}</span>
        <span class="level">{{ log.level }}</span>
        <span class="message">{{ log.message }}</span>
      </div>
    </div>
  </div>
</template>
```

### 日志样式
```scss
.log-viewer {
  .log-line {
    font-family: 'Consolas', monospace;
    padding: 4px 8px;
    border-bottom: 1px solid #eee;
    
    &.ERROR {
      background-color: #fee;
      color: #c00;
    }
    
    &.WARN {
      background-color: #ffc;
      color: #c60;
    }
    
    &.INFO {
      color: #333;
    }
  }
  
  .timestamp {
    color: #999;
    margin-right: 10px;
  }
  
  .level {
    font-weight: bold;
    margin-right: 10px;
  }
}
```

---

## 性能优化

### 1. 日志分页
- 大文件分块读取
- 索引构建加速查询
- 缓存热点日志

### 2. 实时推送优化
- 日志批量发送
- 压缩传输
- 限制推送频率

### 3. 搜索优化
- 关键词索引
- 全文搜索引擎（Elasticsearch）
- 异步搜索

---

## 扩展功能

### 1. 日志分析
- 错误趋势分析
- 性能指标提取
- 异常模式识别

### 2. 日志告警
- 错误日志告警
- 关键词监控
- 邮件/消息通知

### 3. 日志归档
- 自动归档旧日志
- 压缩存储
- 清理策略
