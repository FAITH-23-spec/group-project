package com.polynomialsolver.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableInitializer {
    public static void initializeTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS solved_polynomials (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "polynomial VARCHAR(255) NOT NULL," +
                "solution TEXT NOT NULL," +
                "degree INT NOT NULL," +
                "method_used VARCHAR(50) NOT NULL," +
                "solved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        }
    }
} 