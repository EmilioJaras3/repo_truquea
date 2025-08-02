package com.trukea.controller;

import io.javalin.http.Context;
import com.trukea.ApiResponse;
import com.trukea.config.DatabaseConfig;
import com.trukea.models.Trade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeController {

    public void proposeTrade(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int productoOfrecidoId = getIntegerFromBody(body, "producto_ofrecido_id");
            int productoDeseadoId = getIntegerFromBody(body, "producto_deseado_id");
            int usuarioOferenteId = getIntegerFromBody(body, "usuario_oferente_id");
            String comentario = (String) body.get("comentario");

            if (productoOfrecidoId <= 0 || productoDeseadoId <= 0 || usuarioOferenteId <= 0) {
                ctx.status(400).json(new ApiResponse(false, "IDs de productos y usuario son obligatorios", null));
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection()) {
                // 1. Get owner of desired product
                int usuarioReceptorId;
                try (PreparedStatement getOwnerStmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?")) {
                    getOwnerStmt.setInt(1, productoDeseadoId);
                    ResultSet rs = getOwnerStmt.executeQuery();
                    if (!rs.next()) {
                        ctx.status(400).json(new ApiResponse(false, "Producto deseado no encontrado", null));
                        return;
                    }
                    usuarioReceptorId = rs.getInt("usuario_id");
                }

                if (usuarioOferenteId == usuarioReceptorId) {
                    ctx.status(400).json(new ApiResponse(false, "No puedes intercambiar con tus propios productos", null));
                    return;
                }

                // 2. Verify ownership of offered product
                try (PreparedStatement checkOwnerStmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?")) {
                    checkOwnerStmt.setInt(1, productoOfrecidoId);
                    ResultSet ownerRs = checkOwnerStmt.executeQuery();
                    if (!ownerRs.next()) {
                        ctx.status(400).json(new ApiResponse(false, "Producto ofrecido no encontrado", null));
                        return;
                    }
                    if (ownerRs.getInt("usuario_id") != usuarioOferenteId) {
                        ctx.status(400).json(new ApiResponse(false, "No puedes ofrecer un producto que no te pertenece", null));
                        return;
                    }
                }

                // 3. Insert trade proposal
                String insertSql = "INSERT INTO trueques (producto_ofrecido_id, producto_deseado_id, usuario_oferente_id, usuario_receptor_id, comentario) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, productoOfrecidoId);
                    insertStmt.setInt(2, productoDeseadoId);
                    insertStmt.setInt(3, usuarioOferenteId);
                    insertStmt.setInt(4, usuarioReceptorId);
                    insertStmt.setString(5, comentario);

                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int tradeId = generatedKeys.getInt(1);
                            ctx.status(201).json(new ApiResponse(true, "Propuesta de trueque enviada exitosamente", Map.of("tradeId", tradeId)));
                        }
                    } else {
                        ctx.status(500).json(new ApiResponse(false, "Error al insertar trueque", null));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error de base de datos: " + e.getMessage(), null));
        } catch (Exception e) {

            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error al crear propuesta de trueque: " + e.getMessage(), null));
        }
    }

    private int getIntegerFromBody(Map<String, Object> body, String key) {
        Object value = body.get(key);
        System.out.println("🔍 Convirtiendo " + key + ": " + value + " (tipo: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                System.err.println(" Error convirtiendo " + key + " a entero: " + value);
                return 0;
            }
        }
        System.err.println(" Valor inválido para " + key + ": " + value);
        return 0;
    }

    // ... resto de métodos permanecen igual (getReceivedRequests, getSentRequests, etc.)

    public void getReceivedRequests(Context ctx) {
        String sql = "SELECT t.*, " +
                "po.nombre as producto_ofrecido_nombre, po.imagen as producto_ofrecido_imagen, " +
                "pd.nombre as producto_deseado_nombre, pd.imagen as producto_deseado_imagen, " +
                "u.nombre as oferente_nombre, u.apellido as oferente_apellido, " +
                "et.nombre as estado_nombre " +
                "FROM trueques t " +
                "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                "JOIN usuarios u ON t.usuario_oferente_id = u.id " +
                "JOIN estados_trueque et ON t.estado_id = et.id " +
                "WHERE t.usuario_receptor_id = ? " +
                "ORDER BY t.fecha_propuesta DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            List<Trade> requests = new ArrayList<>();
            while (rs.next()) {
                requests.add(mapResultSetToTrade(rs));
            }

            ctx.json(new ApiResponse(true, "Solicitudes recibidas obtenidas", Map.of("requests", requests)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getSentRequests(Context ctx) {
        String sql = "SELECT t.*, " +
                "po.nombre as producto_ofrecido_nombre, po.imagen as producto_ofrecido_imagen, " +
                "pd.nombre as producto_deseado_nombre, pd.imagen as producto_deseado_imagen, " +
                "u.nombre as receptor_nombre, u.apellido as receptor_apellido, " +
                "et.nombre as estado_nombre " +
                "FROM trueques t " +
                "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                "JOIN usuarios u ON t.usuario_receptor_id = u.id " +
                "JOIN estados_trueque et ON t.estado_id = et.id " +
                "WHERE t.usuario_oferente_id = ? " +
                "ORDER BY t.fecha_propuesta DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            List<Trade> requests = new ArrayList<>();
            while (rs.next()) {
                Trade trade = mapResultSetToTrade(rs);
                trade.setReceptorNombre(rs.getString("receptor_nombre"));
                trade.setReceptorApellido(rs.getString("receptor_apellido"));
                requests.add(trade);
            }

            ctx.json(new ApiResponse(true, "Solicitudes enviadas obtenidas", Map.of("requests", requests)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void acceptTrade(Context ctx) {
        updateTradeStatus(ctx, 2, "Trueque aceptado exitosamente");
    }

    public void rejectTrade(Context ctx) {
        updateTradeStatus(ctx, 3, "Trueque rechazado exitosamente");
    }

    public void completeTrade(Context ctx) {
        updateTradeStatus(ctx, 4, "Trueque completado exitosamente");
    }

    public void cancelTrade(Context ctx) {
        updateTradeStatus(ctx, 5, "Trueque cancelado exitosamente");
    }

    private void updateTradeStatus(Context ctx, int statusId, String successMessage) {
        String sql = "UPDATE trueques SET estado_id = ?, fecha_respuesta = NOW() WHERE id = ?";
        if (statusId == 4) { // Completed
            sql = "UPDATE trueques SET estado_id = ?, fecha_completado = NOW() WHERE id = ?";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));
            stmt.setInt(1, statusId);
            stmt.setInt(2, tradeId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ctx.json(new ApiResponse(true, successMessage, null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar estado del trueque", null));
            e.printStackTrace();
        }
    }

    public void getTradeHistory(Context ctx) {
        String sql = "SELECT t.*, " +
                "po.nombre as producto_ofrecido_nombre, " +
                "pd.nombre as producto_deseado_nombre, " +
                "et.nombre as estado_nombre, " +
                "CASE WHEN t.usuario_oferente_id = ? THEN 'enviado' ELSE 'recibido' END as tipo " +
                "FROM trueques t " +
                "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                "JOIN estados_trueque et ON t.estado_id = et.id " +
                "WHERE t.usuario_oferente_id = ? OR t.usuario_receptor_id = ? " +
                "ORDER BY t.fecha_propuesta DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> history = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> trade = Map.of(
                        "id", rs.getInt("id"),
                        "producto_ofrecido_nombre", rs.getString("producto_ofrecido_nombre"),
                        "producto_deseado_nombre", rs.getString("producto_deseado_nombre"),
                        "estado_nombre", rs.getString("estado_nombre"),
                        "tipo", rs.getString("tipo"),
                        "fecha_propuesta", rs.getTimestamp("fecha_propuesta")
                );
                history.add(trade);
            }

            ctx.json(new ApiResponse(true, "Historial obtenido", Map.of("history", history)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException {
        Trade trade = new Trade();
        trade.setId(rs.getInt("id"));
        trade.setProductoOfrecidoId(rs.getInt("producto_ofrecido_id"));
        trade.setProductoDeseadoId(rs.getInt("producto_deseado_id"));
        trade.setUsuarioOferenteId(rs.getInt("usuario_oferente_id"));
        trade.setUsuarioReceptorId(rs.getInt("usuario_receptor_id"));
        trade.setComentario(rs.getString("comentario"));
        trade.setEstadoId(rs.getInt("estado_id"));

        // Campos adicionales
        trade.setProductoOfrecidoNombre(rs.getString("producto_ofrecido_nombre"));
        trade.setProductoOfrecidoImagen(rs.getString("producto_ofrecido_imagen"));
        trade.setProductoDeseadoNombre(rs.getString("producto_deseado_nombre"));
        trade.setProductoDeseadoImagen(rs.getString("producto_deseado_imagen"));
        trade.setOferenteNombre(rs.getString("oferente_nombre"));
        trade.setOferenteApellido(rs.getString("oferente_apellido"));
        trade.setEstadoNombre(rs.getString("estado_nombre"));

        return trade;
    }
}