# 配置管理功能设计

## 功能概述

管理各微服务的application.yml配置文件，支持多环境配置、在线编辑、配置验证和服务重启。

## 配置文件结构

### 各服务配置文件位置
```
services/
├── anchor-service/src/main/resources/
│   ├── application.yml              # 主配置
│   ├── application-dev.yml          # 开发环境
│   ├── application-test.yml         # 测试环境
│   └── application-prod.yml         # 生产环境
├── audience-service/src/main/resources/
├── finance-service/src/main/resources/
└── ...
```

### 主要配置项
```yaml
# 服务配置
server:
  port: 8081
  servlet:
    context-path: /anchor

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db1
    username: root
    password: root
    
# Redis配置
  redis:
    host: localhost
    port: 6379
    password: ''
    database: 0

# 日志配置
logging:
  level:
    root: INFO
    com.liveroom: DEBUG
```

---

## API接口设计

### 1. 配置查询

#### 1.1 获取服务列表
```
GET /api/v1/config/services
Response: BaseResponse<List<ServiceInfo>>
```

**ServiceInfo**:
```typescript
{
  serviceName: string
  displayName: string
  configPath: string
  profiles: string[]  // dev, test, prod
  currentProfile: string
}
```

#### 1.2 获取服务配置
```
GET /api/v1/config/{serviceName}
Query: profile (dev, test, prod)
Response: BaseResponse<ConfigContent>
```

**ConfigContent**:
```typescript
{
  serviceName: string
  profile: string
  filePath: string
  content: string  // YAML内容
  lastModified: string
}
```

#### 1.3 获取配置项
```
GET /api/v1/config/{serviceName}/property
Query: profile, key
Response: BaseResponse<ConfigProperty>
```

**ConfigProperty**:
```typescript
{
  key: string
  value: any
  type: string
  description: string
}
```

---

### 2. 配置修改

#### 2.1 更新配置文件
```
PUT /api/v1/config/{serviceName}
Query: profile
Body: {
  content: string  // 完整YAML内容
}
Response: BaseResponse<Void>
```

#### 2.2 更新单个配置项
```
PUT /api/v1/config/{serviceName}/property
Query: profile
Body: {
  key: string
  value: any
}
Response: BaseResponse<Void>
```

#### 2.3 批量更新配置
```
PUT /api/v1/config/{serviceName}/batch
Query: profile
Body: {
  properties: Array<{
    key: string
    value: any
  }>
}
Response: BaseResponse<BatchUpdateResult>
```

---

### 3. 环境管理

#### 3.1 获取当前环境
```
GET /api/v1/config/environment
Response: BaseResponse<EnvironmentInfo>
```

**EnvironmentInfo**:
```typescript
{
  currentEnv: string  // dev, test, prod
  availableEnvs: string[]
  services: Array<{
    serviceName: string
    activeProfile: string
    configured: boolean
  }>
}
```

#### 3.2 切换环境
```
POST /api/v1/config/environment/switch
Body: {
  environment: string
  services: string[]  // 要切换的服务
}
Response: BaseResponse<SwitchResult>
```

**SwitchResult**:
```typescript
{
  environment: string
  successServices: string[]
  failedServices: Array<{
    serviceName: string
    reason: string
  }>
}
```

#### 3.3 创建环境配置
```
POST /api/v1/config/environment/create
Body: {
  serviceName: string
  environment: string
  copyFrom: string  // 复制自哪个环境
}
Response: BaseResponse<Void>
```

---

### 4. 配置模板

#### 4.1 获取配置模板
```
GET /api/v1/config/template/{serviceName}
Response: BaseResponse<ConfigTemplate>
```

**ConfigTemplate**:
```typescript
{
  serviceName: string
  sections: Array<{
    name: string
    description: string
    properties: Array<{
      key: string
      type: string
      defaultValue: any
      required: boolean
      description: string
    }>
  }>
}
```

#### 4.2 应用配置模板
```
POST /api/v1/config/template/{serviceName}/apply
Query: environment
Body: {
  values: Record<string, any>
}
Response: BaseResponse<Void>
```

---

### 5. 配置验证

#### 5.1 验证配置语法
```
POST /api/v1/config/validate/syntax
Body: {
  content: string  // YAML内容
}
Response: BaseResponse<ValidationResult>
```

**ValidationResult**:
```typescript
{
  valid: boolean
  errors: Array<{
    line: number
    column: number
    message: string
  }>
}
```

#### 5.2 验证配置完整性
```
POST /api/v1/config/validate/completeness
Body: {
  serviceName: string
  profile: string
  content: string
}
Response: BaseResponse<CompletenessResult>
```

**CompletenessResult**:
```typescript
{
  valid: boolean
  missingRequired: string[]
  warnings: string[]
}
```

---

### 6. 配置备份与恢复

#### 6.1 备份配置
```
POST /api/v1/config/backup
Body: {
  serviceName: string
  profile: string
  description: string
}
Response: BaseResponse<BackupInfo>
```

**BackupInfo**:
```typescript
{
  backupId: string
  serviceName: string
  profile: string
  timestamp: string
  description: string
}
```

#### 6.2 配置备份列表
```
GET /api/v1/config/backups
Query: serviceName, profile
Response: BaseResponse<List<BackupInfo>>
```

#### 6.3 恢复配置
```
POST /api/v1/config/restore/{backupId}
Response: BaseResponse<Void>
```

#### 6.4 导出配置
```
GET /api/v1/config/export/{serviceName}
Query: profile
Response: File download (YAML)
```

#### 6.5 导入配置
```
POST /api/v1/config/import/{serviceName}
Query: profile
Body: Multipart file upload
Response: BaseResponse<Void>
```

---

## 核心实现

### 1. YAML文件读写

#### ConfigFileService
```java
@Service
public class ConfigFileService {
    
    @Value("${config.base-path:./services}")
    private String basePath;
    
    // 读取配置文件
    public String readConfigFile(String serviceName, String profile) {
        Path configPath = buildConfigPath(serviceName, profile);
        try {
            return Files.readString(configPath);
        } catch (IOException e) {
            throw new ConfigException("Failed to read config file", e);
        }
    }
    
    // 写入配置文件
    public void writeConfigFile(String serviceName, String profile, String content) {
        Path configPath = buildConfigPath(serviceName, profile);
        try {
            // 先备份
            backupConfigFile(serviceName, profile);
            // 写入新内容
            Files.writeString(configPath, content);
        } catch (IOException e) {
            throw new ConfigException("Failed to write config file", e);
        }
    }
    
    // 构建配置文件路径
    private Path buildConfigPath(String serviceName, String profile) {
        String fileName = profile.isEmpty() ? 
            "application.yml" : 
            String.format("application-%s.yml", profile);
        return Paths.get(basePath, serviceName, "src", "main", "resources", fileName);
    }
}
```

---

### 2. YAML解析与操作

#### YamlService
```java
@Service
public class YamlService {
    
    private final Yaml yaml = new Yaml();
    
    // 解析YAML
    public Map<String, Object> parseYaml(String content) {
        return yaml.load(content);
    }
    
    // 获取配置项
    public Object getProperty(Map<String, Object> config, String key) {
        String[] keys = key.split("\\.");
        Object current = config;
        
        for (String k : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(k);
            } else {
                return null;
            }
        }
        return current;
    }
    
    // 设置配置项
    public void setProperty(Map<String, Object> config, String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> current = config;
        
        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(
                keys[i], k -> new LinkedHashMap<>()
            );
        }
        current.put(keys[keys.length - 1], value);
    }
    
    // 转换为YAML字符串
    public String toYamlString(Map<String, Object> config) {
        return yaml.dump(config);
    }
}
```

---

### 3. 配置验证

#### ConfigValidator
```java
@Service
public class ConfigValidator {
    
    // 验证YAML语法
    public ValidationResult validateSyntax(String content) {
        ValidationResult result = new ValidationResult();
        try {
            new Yaml().load(content);
            result.setValid(true);
        } catch (YAMLException e) {
            result.setValid(false);
            result.addError(e.getMark().getLine(), 
                           e.getMark().getColumn(), 
                           e.getMessage());
        }
        return result;
    }
    
    // 验证必需配置项
    public CompletenessResult validateCompleteness(
            String serviceName, String content) {
        
        CompletenessResult result = new CompletenessResult();
        Map<String, Object> config = new Yaml().load(content);
        
        // 检查必需配置项
        String[] requiredKeys = getRequiredKeys(serviceName);
        for (String key : requiredKeys) {
            if (getProperty(config, key) == null) {
                result.addMissingRequired(key);
            }
        }
        
        result.setValid(result.getMissingRequired().isEmpty());
        return result;
    }
    
    // 获取必需配置项
    private String[] getRequiredKeys(String serviceName) {
        return new String[]{
            "server.port",
            "spring.application.name",
            "spring.datasource.url",
            "spring.datasource.username"
        };
    }
}
```

---

### 4. 配置备份

#### ConfigBackupService
```java
@Service
public class ConfigBackupService {
    
    @Value("${config.backup-path:./config-backups}")
    private String backupPath;
    
    // 备份配置
    public BackupInfo backup(String serviceName, String profile, String description) {
        String backupId = generateBackupId();
        Path sourceFile = buildConfigPath(serviceName, profile);
        Path backupFile = Paths.get(backupPath, backupId + ".yml");
        
        try {
            Files.createDirectories(backupFile.getParent());
            Files.copy(sourceFile, backupFile);
            
            // 保存备份元数据
            BackupInfo info = new BackupInfo();
            info.setBackupId(backupId);
            info.setServiceName(serviceName);
            info.setProfile(profile);
            info.setTimestamp(LocalDateTime.now());
            info.setDescription(description);
            
            saveBackupMetadata(info);
            return info;
        } catch (IOException e) {
            throw new ConfigException("Failed to backup config", e);
        }
    }
    
    // 恢复配置
    public void restore(String backupId) {
        BackupInfo info = getBackupInfo(backupId);
        Path backupFile = Paths.get(backupPath, backupId + ".yml");
        Path targetFile = buildConfigPath(info.getServiceName(), info.getProfile());
        
        try {
            Files.copy(backupFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ConfigException("Failed to restore config", e);
        }
    }
}
```

---

## 前端展示

### 配置编辑器
```vue
<template>
  <div class="config-editor">
    <div class="toolbar">
      <el-select v-model="selectedService" @change="loadConfig">
        <el-option label="Anchor Service" value="anchor-service" />
      </el-select>
      
      <el-select v-model="selectedProfile" @change="loadConfig">
        <el-option label="Development" value="dev" />
        <el-option label="Test" value="test" />
        <el-option label="Production" value="prod" />
      </el-select>
      
      <el-button @click="saveConfig" type="primary">保存</el-button>
      <el-button @click="validateConfig">验证</el-button>
      <el-button @click="backupConfig">备份</el-button>
      <el-button @click="restartService">重启服务</el-button>
    </div>
    
    <!-- YAML编辑器 -->
    <code-mirror
      v-model="configContent"
      :options="editorOptions"
      @change="onConfigChange"
    />
    
    <!-- 验证结果 -->
    <div v-if="validationErrors.length" class="validation-errors">
      <el-alert
        v-for="error in validationErrors"
        :key="error.line"
        type="error"
        :title="`Line ${error.line}: ${error.message}`"
      />
    </div>
  </div>
</template>
```

### 配置模板助手
```vue
<template>
  <el-dialog title="配置助手" v-model="visible">
    <el-form :model="form">
      <el-form-item label="服务端口">
        <el-input-number v-model="form.port" />
      </el-form-item>
      
      <el-form-item label="数据库地址">
        <el-input v-model="form.dbHost" />
      </el-form-item>
      
      <el-form-item label="Redis地址">
        <el-input v-model="form.redisHost" />
      </el-form-item>
      
      <!-- 更多配置项 -->
    </el-form>
    
    <template #footer>
      <el-button @click="applyTemplate" type="primary">应用</el-button>
    </template>
  </el-dialog>
</template>
```

---

## 安全考虑

1. **敏感信息保护**: 密码等敏感信息加密显示
2. **操作权限**: 只有管理员可修改生产环境配置
3. **变更审计**: 记录所有配置变更日志
4. **自动备份**: 修改前自动备份
5. **回滚机制**: 支持快速回滚到历史版本

---

## 扩展功能

### 1. 配置对比
- 不同环境配置对比
- 配置变更历史对比
- 差异高亮显示

### 2. 配置同步
- 从dev同步到test
- 批量更新多个服务
- 配置模板共享

### 3. 配置监控
- 配置变更通知
- 配置漂移检测
- 配置合规检查
