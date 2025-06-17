package com.example.energy.percentage.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
public class UsageDataSourceConfig {

    @Bean
    @ConfigurationProperties("usage.datasource")
    public DataSourceProperties usageDataSourceProperties() {
        return new DataSourceProperties();
    }



    @Bean(name = "usageDataSource")
    @ConfigurationProperties("usage.datasource")
    public DataSource usageDataSource() {
        return usageDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "usageJdbcTemplate")
    public JdbcTemplate usageJdbcTemplate(@Qualifier("usageDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


}

