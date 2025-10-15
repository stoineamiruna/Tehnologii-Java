package com.example.Lab2.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile({"dev","prod"})
@ConditionalOnExpression("'${db.external:false}' == 'true'")
public class DataSourceExternalConfig {

    @Autowired
    private DbProperties dbProperties;

    @Bean
    public DataSource dataSource() {
        String url = dbProperties.getUrl();
        if (url == null || url.isBlank()) {
            url = dbProperties.getJdbcUrlForMySql();
        }
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url(url)
                .username(dbProperties.getUsername())
                .password(dbProperties.getPassword())
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }
}