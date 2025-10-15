package com.example.Lab2.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("dev")
@ConditionalOnExpression("'${db.external:false}' == 'false'")
public class DataSourceDevConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:lab2_dev;DB_CLOSE_DELAY=-1;MODE=MySQL")
                .username("sa")
                .password("")
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
/*
 * Pentru citirea datelor de configurare ale bazei de date am folosit @ConfigurationProperties
 * in clasa DbProperties. Aceasta permite maparea tuturor proprietatilor relevante (host, port,
 * nume baza de date, utilizator si parola) din fisierele application-{profile}.yml sau din
 * variabilele de mediu catre un singur bean Java.
 *
 * Am ales aceasta abordare deoarece:
 * 1. Centralizeaza configurarea – toate proprietatile legate de DataSource sunt intr-un singur
 *    loc, evitand multiple @Value raspandite in cod.
 * 2. Permite validare si fallback logic – putem implementa usor valori implicite sau generarea
 *    URL-ului JDBC daca acesta nu este specificat.
 * 3. Functioneaza cu profiluri si conditii – combinat cu @Profile si @ConditionalOnExpression,
 *    putem selecta automat configuratia potrivita pentru mediul activ.
 * 4. Usor de extins si intretinut – daca apar noi proprietati sau se schimba mediile,
 *    modificarile se fac doar in fisierul de configurare si clasa DbProperties, fara sa atingem
 *    codul principal.
 *
 * Astfel, utilizarea @ConfigurationProperties face aplicatia mai flexibila, mai curata si mai
 * usor de intretinut comparativ cu @Value pentru fiecare proprietate in parte.
 */
