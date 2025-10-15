package com.example.Lab2.actuator;

import javax.sql.DataSource;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Component
public class DbInfoContributor implements InfoContributor {

    private final DataSource dataSource;
    private final Environment env;

    public DbInfoContributor(DataSource dataSource, Environment env) {
        this.dataSource = dataSource;
        this.env = env;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> dbInfo = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            dbInfo.put("url", conn.getMetaData().getURL());
            dbInfo.put("product", conn.getMetaData().getDatabaseProductName());
            dbInfo.put("user", conn.getMetaData().getUserName());
        } catch (Exception ex) {
            dbInfo.put("error", ex.getMessage());
        }
        dbInfo.put("activeProfiles", env.getActiveProfiles());
        builder.withDetail("database", dbInfo);
    }
}

