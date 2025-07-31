package com.trukea.config;

import java.sql.*;

public class DatabaseConfig {
    private static final String URL = System.getenv("DATABASE_URL") != null
            ? System.getenv("DATABASE_URL")
            : "jdbc:mysql://52.71.195.110:3306/trukea_db";
    private static final String USERNAME = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "admin";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "Angelito7@2024!";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = getConnection();
            System.out.println(" Conectado a la base de datos MySQL");
            conn.close();
        } catch (Exception e) {
            System.err.println(" Error conectando a la base de datos: " + e.getMessage());
        }
    }
}