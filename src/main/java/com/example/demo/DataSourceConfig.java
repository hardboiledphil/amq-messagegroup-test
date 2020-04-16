package com.example.demo;

import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean(name="datasourceODS")
    public DataSource dataSourceODS() {

        AtomikosDataSourceBean dataSourceBeanODS = new AtomikosDataSourceBean();
        dataSourceBeanODS.setUniqueResourceName("oracleODS");
        dataSourceBeanODS.setXaDataSourceClassName("oracle.jdbc.xa.client.OracleXADataSource");

        Properties p = new Properties();
        p.setProperty ( "user" , "OSOWNERBS1" );
        p.setProperty ( "password" , "OSOWNERBS1" );
        p.setProperty ( "URL" , "jdbc:oracle:thin:@REUXEUUX1089:1521:d18lod1" );
        dataSourceBeanODS.setXaProperties( p );

        return dataSourceBeanODS;

    }

    @Bean(name="datasourceGOPS")
    public DataSource dataSourceGOPS() {

        AtomikosDataSourceBean dataSourceBeanGOPS = new AtomikosDataSourceBean();
        dataSourceBeanGOPS.setUniqueResourceName("oracleGOPS");
        dataSourceBeanGOPS.setXaDataSourceClassName("oracle.jdbc.xa.client.OracleXADataSource");

        Properties p = new Properties();
        p.setProperty ( "user" , "GPOWNERBS1" );
        p.setProperty ( "password" , "GPOWNERBS1" );
        p.setProperty ( "URL" , "jdbc:oracle:thin:@REUXEUUX1089:1521:d18lpe1" );
        dataSourceBeanGOPS.setXaProperties( p );

        return dataSourceBeanGOPS;

    }
}
