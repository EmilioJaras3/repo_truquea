// ===================================================================
// TU RatingController.java ACTUALIZADO - SOLO AGREGAR AL FINAL
// ===================================================================
package com.trukea.controller;

import io.javalin.http.Context;
import com.trukea.ApiResponse;
import com.trukea.service.RatingService;
import com.trukea.models.Rating;
import com.trukea.config.DatabaseConfig;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class RatingController {
    private RatingService ratingService;

    public RatingController() {
        this.ratingService = new RatingService();
    }

    // ✅ TUS MÉTODOS EXISTENTES (NO TOCAR)
    public void createRating(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int truequeId = ((Double) body.get("trueque_id")).intValue();
            int calificadorId = ((Double) body.get("calificador_id")).intValue();
            int calificadoId = ((Double) body.get("calificado_id")).intValue();
            int puntuacion = ((Double) body.get("puntuacion")).intValue();
            String comentario = (String) body.get("comentario");

            if (puntuacion < 1 || puntuacion > 5) {
                ctx.status(400).json(new ApiResponse(false, "La puntuación debe estar entre 1 y 5", null));
                return;
            }

            // Verificar si ya existe una calificación
            if (ratingService.hasUserRatedTrade(truequeId, calificadorId)) {
                ctx.status(400).json(new ApiResponse(false, "Ya has calificado este trueque", null));
                return;
            }

            int ratingId = ratingService.createRating(truequeId, calificadorId, calificadoId, puntuacion, comentario);

            if (ratingId > 0) {
                ctx.status(201).json(new ApiResponse(true, "Calificación enviada exitosamente",
                        Map.of("ratingId", ratingId)));
            } else {
                ctx.status(500).json(new ApiResponse(false, "Error al crear calificación", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getUserRatings(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Rating> ratings = ratingService.getUserRatings(userId);
            ctx.json(new ApiResponse(true, "Calificaciones obtenidas", Map.of("ratings", ratings)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getPendingRatings(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            // Esta funcionalidad la implementaremos cuando esté lista en RatingService
            ctx.json(new ApiResponse(true, "Trueques pendientes de calificar obtenidos",
                    Map.of("pending_ratings", List.of())));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    // ===================================================================
    // 🚀 NUEVOS MÉTODOS - AGREGAR ESTOS AL FINAL DE TU ARCHIVO
    // ===================================================================

    public void getTopUsers(Context ctx) {
        try {
            String order = ctx.queryParam("order");
            String limit = ctx.queryParam("limit");

            if (order == null) order = "desc";
            if (limit == null) limit = "10";

            String orderBy = order.equals("asc") ? "ASC" : "DESC";

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT u.id, u.nombre, u.apellido, u.calificacion_promedio, " +
                            "COUNT(c.id) as total_calificaciones, " +
                            "COUNT(DISTINCT t.id) as total_trueques " +
                            "FROM usuarios u " +
                            "LEFT JOIN calificaciones c ON u.id = c.calificado_id " +
                            "LEFT JOIN trueques t ON (u.id = t.usuario_oferente_id OR u.id = t.usuario_receptor_id) " +
                            "WHERE u.calificacion_promedio > 0 " +
                            "GROUP BY u.id, u.nombre, u.apellido, u.calificacion_promedio " +
                            "HAVING total_calificaciones > 0 " +
                            "ORDER BY u.calificacion_promedio " + orderBy + ", total_calificaciones " + orderBy + " " +
                            "LIMIT ?"
            );
            stmt.setInt(1, Integer.parseInt(limit));

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> topUsers = new ArrayList<>();

            int posicion = 1;
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("posicion", posicion++);
                user.put("id", rs.getInt("id"));
                user.put("nombre", rs.getString("nombre"));
                user.put("apellido", rs.getString("apellido"));
                user.put("nombre_completo", rs.getString("nombre") + " " + rs.getString("apellido"));
                user.put("calificacion_promedio", rs.getDouble("calificacion_promedio"));
                user.put("total_calificaciones", rs.getInt("total_calificaciones"));
                user.put("total_trueques", rs.getInt("total_trueques"));
                topUsers.add(user);
            }

            String mensaje = order.equals("desc") ?
                    "Usuarios mejor calificados" :
                    "Usuarios con menor calificación";

            ctx.json(new ApiResponse(true, mensaje, Map.of("ranking", topUsers)));
            conn.close();

        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getMostActiveUsers(Context ctx) {
        try {
            String limit = ctx.queryParam("limit");
            if (limit == null) limit = "10";

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT u.id, u.nombre, u.apellido, u.calificacion_promedio, " +
                            "COUNT(DISTINCT CASE WHEN t.estado_id = 4 THEN t.id END) as trueques_completados, " +
                            "COUNT(DISTINCT p.id) as total_productos, " +
                            "COUNT(DISTINCT c.id) as calificaciones_dadas, " +
                            "(COUNT(DISTINCT CASE WHEN t.estado_id = 4 THEN t.id END) * 3 + " +
                            " COUNT(DISTINCT p.id) * 2 + " +
                            " COUNT(DISTINCT c.id) * 1) as puntuacion_actividad " +
                            "FROM usuarios u " +
                            "LEFT JOIN trueques t ON (u.id = t.usuario_oferente_id OR u.id = t.usuario_receptor_id) " +
                            "LEFT JOIN productos p ON u.id = p.usuario_id " +
                            "LEFT JOIN calificaciones c ON u.id = c.calificador_id " +
                            "GROUP BY u.id, u.nombre, u.apellido, u.calificacion_promedio " +
                            "HAVING puntuacion_actividad > 0 " +
                            "ORDER BY puntuacion_actividad DESC, trueques_completados DESC " +
                            "LIMIT ?"
            );
            stmt.setInt(1, Integer.parseInt(limit));

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> activeUsers = new ArrayList<>();

            int posicion = 1;
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("posicion", posicion++);
                user.put("id", rs.getInt("id"));
                user.put("nombre", rs.getString("nombre"));
                user.put("apellido", rs.getString("apellido"));
                user.put("nombre_completo", rs.getString("nombre") + " " + rs.getString("apellido"));
                user.put("calificacion_promedio", rs.getDouble("calificacion_promedio"));
                user.put("trueques_completados", rs.getInt("trueques_completados"));
                user.put("total_productos", rs.getInt("total_productos"));
                user.put("calificaciones_dadas", rs.getInt("calificaciones_dadas"));
                user.put("puntuacion_actividad", rs.getInt("puntuacion_actividad"));
                activeUsers.add(user);
            }

            ctx.json(new ApiResponse(true, "Usuarios más activos", Map.of("ranking", activeUsers)));
            conn.close();

        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getStatistics(Context ctx) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            Map<String, Object> stats = new HashMap<>();

            // Estadísticas básicas
            PreparedStatement basicStmt = conn.prepareStatement(
                    "SELECT " +
                            "(SELECT COUNT(*) FROM usuarios) as total_usuarios, " +
                            "(SELECT COUNT(*) FROM productos WHERE disponible = true) as productos_disponibles, " +
                            "(SELECT COUNT(*) FROM trueques) as total_trueques, " +
                            "(SELECT COUNT(*) FROM trueques WHERE estado_id = 4) as trueques_completados, " +
                            "(SELECT COUNT(*) FROM calificaciones) as total_calificaciones, " +
                            "(SELECT COALESCE(AVG(puntuacion), 0) FROM calificaciones) as calificacion_promedio"
            );

            ResultSet basicRs = basicStmt.executeQuery();
            if (basicRs.next()) {
                stats.put("total_usuarios", basicRs.getInt("total_usuarios"));
                stats.put("productos_disponibles", basicRs.getInt("productos_disponibles"));
                stats.put("total_trueques", basicRs.getInt("total_trueques"));
                stats.put("trueques_completados", basicRs.getInt("trueques_completados"));
                stats.put("total_calificaciones", basicRs.getInt("total_calificaciones"));
                stats.put("calificacion_promedio_general", Math.round(basicRs.getDouble("calificacion_promedio") * 100.0) / 100.0);
            }

            // Top categorías
            PreparedStatement catStmt = conn.prepareStatement(
                    "SELECT c.nombre, COUNT(p.id) as cantidad " +
                            "FROM categorias c " +
                            "LEFT JOIN productos p ON c.id = p.categoria_id AND p.disponible = true " +
                            "GROUP BY c.id, c.nombre " +
                            "ORDER BY cantidad DESC " +
                            "LIMIT 5"
            );

            ResultSet catRs = catStmt.executeQuery();
            List<Map<String, Object>> topCategorias = new ArrayList<>();

            while (catRs.next()) {
                Map<String, Object> categoria = new HashMap<>();
                categoria.put("nombre", catRs.getString("nombre"));
                categoria.put("cantidad_productos", catRs.getInt("cantidad"));
                topCategorias.add(categoria);
            }

            stats.put("top_categorias", topCategorias);

            ctx.json(new ApiResponse(true, "Estadísticas generales", stats));
            conn.close();

        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }
}