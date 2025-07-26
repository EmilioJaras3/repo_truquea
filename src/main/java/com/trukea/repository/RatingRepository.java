package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RatingRepository {

    public boolean existsByTradeAndUser(int tradeId, int userId) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM calificaciones WHERE trueque_id = ? AND calificador_id = ?"
            );
            stmt.setInt(1, tradeId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            boolean exists = rs.next();
            conn.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int save(Rating rating) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO calificaciones (trueque_id, calificador_id, calificado_id, puntuacion, comentario) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setInt(1, rating.getTruequeId());
            stmt.setInt(2, rating.getCalificadorId());
            stmt.setInt(3, rating.getCalificadoId());
            stmt.setInt(4, rating.getPuntuacion());
            stmt.setString(5, rating.getComentario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int ratingId = generatedKeys.getInt(1);
                    conn.close();
                    return ratingId;
                }
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Rating> findByUserId(int userId) {
        List<Rating> ratings = new ArrayList<>();
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.*, u.nombre as calificador_nombre, u.apellido as calificador_apellido " +
                            "FROM calificaciones c " +
                            "JOIN usuarios u ON c.calificador_id = u.id " +
                            "WHERE c.calificado_id = ? " +
                            "ORDER BY c.created_at DESC"
            );
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setTruequeId(rs.getInt("trueque_id"));
                rating.setCalificadorId(rs.getInt("calificador_id"));
                rating.setCalificadoId(rs.getInt("calificado_id"));
                rating.setPuntuacion(rs.getInt("puntuacion"));
                rating.setComentario(rs.getString("comentario"));
                rating.setCalificadorNombre(rs.getString("calificador_nombre"));
                rating.setCalificadorApellido(rs.getString("calificador_apellido"));

                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    rating.setCreatedAt(timestamp.toLocalDateTime());
                }

                ratings.add(rating);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }

    public List<Map<String, Object>> findPendingRatingsByUserId(int userId) {
        List<Map<String, Object>> pendingRatings = new ArrayList<>();
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.*, " +
                            "po.nombre as producto_ofrecido_nombre, " +
                            "pd.nombre as producto_deseado_nombre, " +
                            "CASE WHEN t.usuario_oferente_id = ? THEN t.usuario_receptor_id ELSE t.usuario_oferente_id END as usuario_a_calificar_id, " +
                            "CASE WHEN t.usuario_oferente_id = ? THEN CONCAT(ur.nombre, ' ', ur.apellido) ELSE CONCAT(uo.nombre, ' ', uo.apellido) END as usuario_a_calificar_nombre " +
                            "FROM trueques t " +
                            "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                            "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                            "JOIN usuarios uo ON t.usuario_oferente_id = uo.id " +
                            "JOIN usuarios ur ON t.usuario_receptor_id = ur.id " +
                            "WHERE t.estado_id = 4 " +
                            "AND (t.usuario_oferente_id = ? OR t.usuario_receptor_id = ?) " +
                            "AND NOT EXISTS (" +
                            "    SELECT 1 FROM calificaciones c " +
                            "    WHERE c.trueque_id = t.id AND c.calificador_id = ?" +
                            ") " +
                            "ORDER BY t.fecha_completado DESC"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> rating = new HashMap<>();
                rating.put("id", rs.getInt("id"));
                rating.put("producto_ofrecido_nombre", rs.getString("producto_ofrecido_nombre"));
                rating.put("producto_deseado_nombre", rs.getString("producto_deseado_nombre"));
                rating.put("usuario_a_calificar_id", rs.getInt("usuario_a_calificar_id"));
                rating.put("usuario_a_calificar_nombre", rs.getString("usuario_a_calificar_nombre"));
                rating.put("fecha_completado", rs.getTimestamp("fecha_completado"));
                pendingRatings.add(rating);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingRatings;
    }
}