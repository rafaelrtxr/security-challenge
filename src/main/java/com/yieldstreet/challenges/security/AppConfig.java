package com.yieldstreet.challenges.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://mysql:3306/yieldstreet");
        //FIXME: Hardcoded credentials (https://cwe.mitre.org/data/definitions/798.html)
        dataSource.setUsername("yieldstreet");
        dataSource.setPassword("yieldstreet");

        return dataSource;
    }
}
