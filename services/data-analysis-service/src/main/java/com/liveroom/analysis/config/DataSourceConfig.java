package com.liveroom.analysis.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 双数据源配置
 * DB1: 观众服务数据库 (live_audience_db)
 * DB2: 财务分析数据库 (live_finance_db)
 */
@Configuration
public class DataSourceConfig {

    /**
     * DB1数据源 - 观众服务数据库
     */
    @Primary
    @Bean(name = "db1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource db1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * DB2数据源 - 财务分析数据库
     */
    @Bean(name = "db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource db2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * DB1 JdbcTemplate
     */
    @Primary
    @Bean(name = "db1JdbcTemplate")
    public JdbcTemplate db1JdbcTemplate(@Qualifier("db1DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * DB2 JdbcTemplate
     */
    @Bean(name = "db2JdbcTemplate")
    public JdbcTemplate db2JdbcTemplate(@Qualifier("db2DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
