package com.wineshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    // Tests if the application can establish a database connection
    @Test
    public void testConnection() throws SQLException {
        try(Connection connection = dataSource.getConnection()){
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(2)).isTrue();
        }
    }
}
