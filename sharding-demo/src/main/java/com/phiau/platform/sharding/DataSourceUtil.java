package com.phiau.platform.sharding;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author phiau
 * @date 2023.08.15 17:34
 */
public class DataSourceUtil {

    public static DataSource createDataSource(DataSourceConfig config) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", config.getHost(), config.getPort(), config.getName()));
        dataSource.setUsername(config.getUserName());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }
}
