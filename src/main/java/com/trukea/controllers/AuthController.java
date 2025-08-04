package com.trukea.controllers;

import io.javalin.http.Context;
import com.google.gson.Gson;
import com.trukea.ApiResponse;
import com.trukea.config.DatabaseConfig;
import com.trukea.models.User;
import java.sql.*;
import java.util.Map;

public class AuthController {
    private Gson gson = new Gson();

    public void register(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String name = (String) body.get("name");
            String lastName = (String) body.get("lastName");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String confirmPassword = (String) body.get("confirmPassword");

            if (name == null || lastName == null || email == null || password == null) {
                ctx.json(new ApiResponse(false, "Todos los campos son obligatorios", null));
                return;
            }
            if (!password.equals(confirmPassword)) {
                ctx.json(new ApiResponse(false, "Las contraseñas no coinciden", null));
                return;
            }
            if (password.length() < 6) {
                ctx.json(new ApiResponse(false, "La contraseña debe tener al menos 6 caracteres", null));
                return;
            }

            String checkEmailSql = "SELECT id FROM usuarios WHERE email = ?";
            String insertUserSql = "INSERT INTO usuarios (nombre, apellido, email, password) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.getConnection()) {
                // Verificar si el email ya existe
                try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
                    checkStmt.setString(1, email);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        ctx.json(new ApiResponse(false, "El email ya está registrado", null));
                        return;
                    }
                }

                // Insertar nuevo usuario
                try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, lastName);
                    insertStmt.setString(3, email);
                    insertStmt.setString(4, password);

                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);
                            ctx.status(201).json(new ApiResponse(true, "Usuario registrado exitosamente",
                                    Map.of("userId", userId)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void login(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String correo = (String) body.get("correo");
            String contrasena = (String) body.get("contrasena");

            if (correo == null || contrasena == null) {
                ctx.status(400).json(new ApiResponse(false, "Email y contraseña son obligatorios", null));
                return;
            }

            String sql = "SELECT * FROM usuarios WHERE email = ? AND password = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, correo);
                stmt.setString(2, contrasena);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNombre(rs.getString("nombre"));
                    user.setApellido(rs.getString("apellido"));
                    user.setEmail(rs.getString("email"));

                    ctx.json(new ApiResponse(true, "Inicio de sesión exitoso", Map.of("user", user)));
                } else {
                    ctx.status(401).json(new ApiResponse(false, "Credenciales inválidas", null));
                }
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }
}