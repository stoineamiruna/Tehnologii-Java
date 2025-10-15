package com.example.Lab2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.datasource")
public class DbProperties {
    private String host;
    private Integer port;
    private String name;
    private String username;
    private String password;
    private String url;

    public String getJdbcUrlForMySql() {
        if (url != null && !url.isBlank()) return url;
        String host0 = (host == null || host.isBlank()) ? "localhost" : host;
        Integer p = (port == null) ? 3306 : port;
        String dbname = (name == null || name.isBlank()) ? "" : name;
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host0, p, dbname);
    }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}