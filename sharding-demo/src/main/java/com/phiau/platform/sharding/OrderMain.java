package com.phiau.platform.sharding;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * @author phiau
 * @date 2023.08.15 18:24
 */
public class OrderMain {

    public static void createTable(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS t_u_order (order_id VARCHAR(50), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";

            Statement statement = connection.createStatement();
            statement.execute(sql);
        }
    }

    public static void insertData(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO t_u_order (order_id, user_id, address_id, status) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            List<String> monthPrefix = Arrays.asList("202308", "202309", "202310");

            for (String m : monthPrefix) {
                for (int i = 15; i < 20; i++) {
                    preparedStatement.setString(1, m + "0" + i);
                    preparedStatement.setInt(2, 1 + i);
                    preparedStatement.setLong(3, 12 + i);
                    preparedStatement.setString(4, "s" + i);

                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        DataSource dataSource = ShardingDatabaseAndTableConfiguration.getDataSource();

        createTable(dataSource);
        insertData(dataSource);
    }
}
