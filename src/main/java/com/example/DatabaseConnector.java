package com.example;

import java.sql.*;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/links_db";

    public static Connection getConnection(String user, String password) throws SQLException {
        return DriverManager.getConnection(DB_URL, user, password);
    }

    public static void executeProcedure(Connection conn, String sql, Object... params) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.execute();
        }
    }
}