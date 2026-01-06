package com.liveroom.dbservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import common.logger.AppLogger;

import javax.sql.DataSource;

/**
 * 多数据源配置
 * 支持同时连接DB1（观众服务）和DB2（财务服务）
 */
@Configuration
public class DataSourceConfig {

    /**
     * 主数据源（DB1: 观众服务数据库）
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        AppLogger.logServiceInitialize("primary-datasource (DB1: live_audience_db)");
        HikariDataSource dataSource = new HikariDataSource(new HikariConfig());
        AppLogger.logServiceInitializeComplete("primary-datasource");
        return dataSource;
    }

    /**
     * 从数据源（DB2: 财务服务数据库）
     */
    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        AppLogger.logServiceInitialize("secondary-datasource (DB2: live_finance_db)");
        HikariDataSource dataSource = new HikariDataSource(new HikariConfig());
        AppLogger.logServiceInitializeComplete("secondary-datasource");
        return dataSource;
    }
}
