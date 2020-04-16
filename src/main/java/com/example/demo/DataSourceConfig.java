package com.example.demo;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSourceODS() {
        return DataSourceBuilder.create()
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@REUXEUUX1089:1521:d18lod1")
                .username("OSOWNERBS1")
                .password("OSOWNERBS1")
                .build();
    }

    @Bean
    public DataSource dataSourceGOPS() {
        return DataSourceBuilder.create()
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@REUXEUUX1089:1521:d18lpe1")
                .username("GPOWNERBS1")
                .password("GPOWNERBS1")
                .build();
    }
}
