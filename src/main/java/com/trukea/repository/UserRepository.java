package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.User;
import java.sql.*;

public class UserRepository {

    public User findByEmail(String email) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuarios WHERE email = ?");
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.close();
                return user;
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByEmailAndPassword(String email, String password) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuarios WHERE email = ? AND password = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.close();
                return user;
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int id) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT u.*, c.nombre as ciudad_nombre FROM usuarios u " +
                            "LEFT JOIN ciudades c ON u.ciudad_id = c.id WHERE u.id = ?"
            );
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                user.setCiudadNombre(rs.getString("ciudad_nombre"));
                conn.close();
                return user;
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int save(User user) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO usuarios (nombre, apellido, email, password) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, user.getNombre());
            stmt.setString(2, user.getApellido());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    conn.close();
                    return userId;
                }
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(int id, User user) {
        try {
            StringBuilder sql = new StringBuilder("UPDATE usuarios SET ");
            boolean hasFields = false;

            if (user.getNombre() != null && !user.getNombre().isEmpty()) {
                sql.append("nombre = ?, ");
                hasFields = true;
            }
            if (user.getApellido() != null && !user.getApellido().isEmpty()) {
                sql.append("apellido = ?, ");
                hasFields = true;
            }
            if (user.getCiudadId() != 0) {
                sql.append("ciudad_id = ?, ");
                hasFields = true;
            }
            if (user.getFechaNacimiento() != null) {
                sql.append("fecha_nacimiento = ?, ");
                hasFields = true;
            }
            if (user.getImagenPerfil() != null && !user.getImagenPerfil().isEmpty()) {
                sql.append("imagen_perfil = ?, ");
                hasFields = true;
            }

            if (!hasFields) {
                return false;
            }

            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ?");

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (user.getNombre() != null && !user.getNombre().isEmpty()) {
                stmt.setString(paramIndex++, user.getNombre());
            }
            if (user.getApellido() != null && !user.getApellido().isEmpty()) {
                stmt.setString(paramIndex++, user.getApellido());
            }
            if (user.getCiudadId() != 0) {
                stmt.setInt(paramIndex++, user.getCiudadId());
            }
            if (user.getFechaNacimiento() != null) {
                stmt.setDate(paramIndex++, Date.valueOf(user.getFechaNacimiento()));
            }
            if (user.getImagenPerfil() != null && !user.getImagenPerfil().isEmpty()) {
                stmt.setString(paramIndex++, user.getImagenPerfil());
            }
            stmt.setInt(paramIndex, id);

            int affectedRows = stmt.executeUpdate();
            conn.close();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateRating(int userId) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE usuarios SET calificacion_promedio = (" +
                            "    SELECT AVG(puntuacion) FROM calificaciones WHERE calificado_id = ?" +
                            ") WHERE id = ?"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            conn.close();
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