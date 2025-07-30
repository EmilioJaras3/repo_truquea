package com.trukea.config;

import java.sql.*;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://52.71.195.110:3306/trukea_db ";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Angelito7@2024!"; // Cambiar por tu contraseña de MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = getConnection();
            System.out.println("✅ Conectado a la base de datos MySQL");
            conn.close();
        } catch (Exception e) {
            System.err.println(" Error conectando a la base de datos: " + e.getMessage());
        }
    }
}