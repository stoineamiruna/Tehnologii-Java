package com.example.Lab2.runner;

import com.example.Lab2.config.DbProperties;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DbPrinterRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Environment env;
    private final DbProperties dbProperties;

    public DbPrinterRunner(JdbcTemplate jdbcTemplate, DataSource dataSource, Environment env, DbProperties dbProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.env = env;
        this.dbProperties = dbProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Connected to DB: " + conn.getMetaData().getURL());
            System.out.println("DB product: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("DB user: " + conn.getMetaData().getUserName());
        } catch (Exception ex) {
            System.err.println("Cannot get DB metadata: " + ex.getMessage());
        }

        // Cream un tabel simplu daca nu exista si inseram cateva randuri
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS person (id INT PRIMARY KEY, name VARCHAR(100))");

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM person", Integer.class);
        if (count == 0) {
            jdbcTemplate.update("INSERT INTO person(id, name) VALUES (?, ?)", 1, "Alice");
            jdbcTemplate.update("INSERT INTO person(id, name) VALUES (?, ?)", 2, "Bob");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, name FROM person");
        System.out.println("Persons in DB:");
        for (Map<String, Object> r : rows) {
            System.out.println(" - id=" + r.get("id") + ", name=" + r.get("name"));
        }

        // Afișam valorile citite de la ConfigurationProperties
        System.out.println("DbProperties (from app.datasource): host=" + dbProperties.getHost()
                + ", port=" + dbProperties.getPort()
                + ", name=" + dbProperties.getName()
                + ", user=" + dbProperties.getUsername());
    }
}
