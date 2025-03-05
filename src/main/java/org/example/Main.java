package org.example;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/links_db";
    private static final String USER = "admin";
    private static final String PASSWORD = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

public class Main {
    public static void main(String[] args) {
        try (var conn = DatabaseConnection.connect();) {
            CallableStatement stmt = conn.prepareCall("{CALL shared.create_links_table()}");
            stmt.execute();
            System.out.println("База данных успешно создана!");

            CallableStatement stmtCreate = conn.prepareCall("{CALL shared.create_link(?, ?)}");
            stmtCreate.setString(1, "https://example.com");
            stmtCreate.setString(2, "Example description");
            stmtCreate.execute();
            System.out.println("Create database successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}