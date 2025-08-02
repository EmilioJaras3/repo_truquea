package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT u.*, c.nombre as ciudad_nombre FROM usuarios u " +
                "LEFT JOIN ciudades c ON u.ciudad_id = c.id WHERE u.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                user.setCiudadNombre(rs.getString("ciudad_nombre"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int save(User user) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getNombre());
            stmt.setString(2, user.getApellido());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(int id, User user) {
        StringBuilder sql = new StringBuilder("UPDATE usuarios SET ");
        List<Object> params = new ArrayList<>();
        if (user.getNombre() != null && !user.getNombre().isEmpty()) {
            sql.append("nombre = ?, ");
            params.add(user.getNombre());
        }
        if (user.getApellido() != null && !user.getApellido().isEmpty()) {
            sql.append("apellido = ?, ");
            params.add(user.getApellido());
        }
        if (user.getCiudadId() != 0) {
            sql.append("ciudad_id = ?, ");
            params.add(user.getCiudadId());
        }
        if (user.getFechaNacimiento() != null) {
            sql.append("fecha_nacimiento = ?, ");
            params.add(Date.valueOf(user.getFechaNacimiento()));
        }
        if (user.getImagenPerfil() != null && !user.getImagenPerfil().isEmpty()) {
            sql.append("imagen_perfil = ?, ");
            params.add(user.getImagenPerfil());
        }

        if (params.isEmpty()) {
            return false;
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateRating(int userId) {
        String sql = "UPDATE usuarios SET calificacion_promedio = " +
                "(SELECT AVG(puntuacion) FROM calificaciones WHERE calificado_id = ?) " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNombre(rs.getString("nombre"));
        user.setApellido(rs.getString("apellido"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCiudadId(rs.getInt("ciudad_id"));
        user.setImagenPerfil(rs.getString("imagen_perfil"));
        user.setCalificacionPromedio(rs.getDouble("calificacion_promedio"));

        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            user.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }

        return user;
    }
}