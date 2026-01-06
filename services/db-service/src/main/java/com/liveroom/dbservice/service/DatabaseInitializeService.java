package com.liveroom.dbservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import common.logger.AppLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;

/**
 * 数据库初始化服务
 * 负责在应用启动时检查和初始化数据库
 */
@Service
@RequiredArgsConstructor
public class DatabaseInitializeService implements CommandLineRunner {

    private final DataSource primaryDataSource;

    @Override
    public void run(String... args) throws Exception {
        AppLogger.info("========== 开始数据库初始化检查 ==========");

        try {
            checkDatabaseConnection();
            checkDatabaseTables();
            AppLogger.logDatabaseInitializeComplete("primary", 11);
            AppLogger.info("========== 数据库初始化检查完成 ==========");
        } catch (Exception e) {
            AppLogger.error("数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查数据库连接
     */
    public void checkDatabaseConnection() throws SQLException {
        try (Connection connection = primaryDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();

            AppLogger.logDatabaseConnection("primary", true,
                    "数据库: " + dbName + ", 版本: " + dbVersion + ", URL: " + metaData.getURL());

            AppLogger.info("数据库用户: {}", metaData.getUserName());
        } catch (SQLException e) {
            AppLogger.error("数据库连接失败", e);
            throw e;
        }
    }

    /**
     * 检查关键表是否存在
     */
    public void checkDatabaseTables() throws SQLException {
        String[] requiredTables = {
                "user", "anchor", "audience",
                "live_room", "live_room_realtime",
                "recharge", "message", "live_session_audience",
                "tag", "user_tag", "tag_relation"
        };

        int existCount = 0;

        try (Connection connection = primaryDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            for (String tableName : requiredTables) {
                var resultSet = metaData.getTables(
                        connection.getCatalog(),
                        null,
                        tableName,
                        new String[] { "TABLE" });

                if (resultSet.next()) {
                    AppLogger.logTableCheck(tableName, true, "已存在");
                    existCount++;
                } else {
                    AppLogger.logTableCheck(tableName, false, "不存在，将由Flyway自动创建");
                }
            }

            AppLogger.info("数据库表检查完成 - 已存在: {}/{}", existCount, requiredTables.length);
        }
    }

    /**
     * 执行初始化SQL脚本（支持本地和远程部署）
     * 优先使用ClassPath资源（支持JAR部署），其次使用文件系统
     */
    public void executeSqlScript(String scriptPath) throws SQLException {
        AppLogger.debug("执行SQL脚本: {}", scriptPath);
        String sqlContent = null;
        
        try {
            // 方案1: 尝试从ClassPath加载（支持JAR包部署）
            try {
                ClassPathResource resource = new ClassPathResource(scriptPath);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        sqlContent = new String(inputStream.readAllBytes());
                        AppLogger.debug("从ClassPath加载SQL脚本: {}", scriptPath);
                    }
                }
            } catch (IOException e) {
                AppLogger.debug("ClassPath中未找到脚本: {}, 尝试文件系统...", scriptPath);
            }
            
            // 方案2: 如果ClassPath未找到，尝试文件系统加载（本地开发环境）
            if (sqlContent == null) {
                try {
                    sqlContent = Files.readString(Paths.get(scriptPath));
                    AppLogger.debug("从文件系统加载SQL脚本: {}", scriptPath);
                } catch (IOException e) {
                    AppLogger.warn("文件系统中也未找到脚本: {}", scriptPath);
                    throw new IOException("无法加载SQL脚本: " + scriptPath, e);
                }
            }
            
            // 执行SQL语句
            try (Connection connection = primaryDataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                
                // 分割SQL语句并执行
                String[] sqlStatements = sqlContent.split(";");
                int successCount = 0;
                int failureCount = 0;
                
                for (String sql : sqlStatements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        try {
                            statement.execute(trimmedSql);
                            successCount++;
                            AppLogger.debug("SQL语句执行成功: {}", 
                                    trimmedSql.substring(0, Math.min(50, trimmedSql.length())));
                        } catch (SQLException e) {
                            failureCount++;
                            AppLogger.warn("SQL语句执行失败: {}, 错误: {}", 
                                    trimmedSql.substring(0, Math.min(50, trimmedSql.length())), 
                                    e.getMessage());
                        }
                    }
                }
                
                AppLogger.info("SQL脚本执行完成: {}, 成功: {}, 失败: {}", 
                        scriptPath, successCount, failureCount);
            }
        } catch (IOException e) {
            AppLogger.error("读取SQL脚本文件失败: {}", scriptPath, e);
            throw new SQLException("SQL脚本执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 初始化数据库表（支持本地和远程部署）
     * SQL脚本应放在 resources/sql 目录下，构建时会打包到JAR
     */
    public void initializeTables() throws SQLException {
        AppLogger.info("========== 开始初始化数据库表 ==========");
        try {
            // ClassPath方案（推荐，支持JAR部署）
            String[] sqlScriptsClassPath = {
                    "sql/01-init-db1-audience-service.sql",
                    "sql/02-init-db2-finance-service.sql"
            };
            
            // 文件系统方案（备用，用于本地开发）
            String[] sqlScriptsFilePath = {
                    "services/db-service/src/main/sql/01-init-db1-audience-service.sql",
                    "services/db-service/src/main/sql/02-init-db2-finance-service.sql"
            };
            
            boolean hasExecutedAny = false;
            
            // 优先使用ClassPath方案
            for (String scriptPath : sqlScriptsClassPath) {
                try {
                    ClassPathResource resource = new ClassPathResource(scriptPath);
                    if (resource.exists()) {
                        executeSqlScript(scriptPath);
                        hasExecutedAny = true;
                    }
                } catch (Exception e) {
                    AppLogger.debug("ClassPath脚本执行失败，将尝试文件系统方案: {}", scriptPath);
                }
            }
            
            // 如果ClassPath方案未成功，尝试文件系统方案
            if (!hasExecutedAny) {
                AppLogger.info("使用文件系统方案加载SQL脚本...");
                for (String scriptPath : sqlScriptsFilePath) {
                    try {
                        executeSqlScript(scriptPath);
                        hasExecutedAny = true;
                    } catch (SQLException e) {
                        AppLogger.warn("脚本执行失败，继续处理下一个: {}", scriptPath);
                    }
                }
            }
            
            if (hasExecutedAny) {
                AppLogger.info("========== 数据库表初始化完成 ==========");
            } else {
                AppLogger.warn("========== 未找到SQL脚本，跳过初始化 ==========");
            }
        } catch (Exception e) {
            AppLogger.error("初始化数据库表失败", e);
            throw new SQLException("初始化数据库表失败: " + e.getMessage(), e);
        }
    }
}
