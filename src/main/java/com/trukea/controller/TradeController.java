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
            int productoOfrecidoId = ((Double) body.get("producto_ofrecido_id")).intValue();
            int productoDeseadoId = ((Double) body.get("producto_deseado_id")).intValue();
            int usuarioOferenteId = ((Double) body.get("usuario_oferente_id")).intValue();
            String comentario = (String) body.get("comentario");

            // Obtener el usuario receptor del producto deseado
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement getOwnerStmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?");
            getOwnerStmt.setInt(1, productoDeseadoId);
            ResultSet rs = getOwnerStmt.executeQuery();

            if (!rs.next()) {
                ctx.status(400).json(new ApiResponse(false, "Producto no encontrado", null));
                conn.close();
                return;
            }

            int usuarioReceptorId = rs.getInt("usuario_id");

            // Insertar propuesta de trueque
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO trueques (producto_ofrecido_id, producto_deseado_id, usuario_oferente_id, usuario_receptor_id, comentario) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
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
                    ctx.status(201).json(new ApiResponse(true, "Propuesta de trueque enviada exitosamente",
                            Map.of("tradeId", tradeId)));
                }
            }

            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al crear propuesta de trueque", null));
            e.printStackTrace();
        }
    }

    public void getReceivedRequests(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.*, " +
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
                            "ORDER BY t.fecha_propuesta DESC"
            );
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            List<Trade> requests = new ArrayList<>();
            while (rs.next()) {
                requests.add(mapResultSetToTrade(rs));
            }

            ctx.json(new ApiResponse(true, "Solicitudes recibidas obtenidas", Map.of("requests", requests)));
            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getSentRequests(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.*, " +
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
                            "ORDER BY t.fecha_propuesta DESC"
            );
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
            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void acceptTrade(Context ctx) {
        try {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE trueques SET estado_id = 2, fecha_respuesta = NOW() WHERE id = ?"
            );
            stmt.setInt(1, tradeId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ctx.json(new ApiResponse(true, "Trueque aceptado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }

            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al aceptar trueque", null));
            e.printStackTrace();
        }
    }

    public void rejectTrade(Context ctx) {
        try {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE trueques SET estado_id = 3, fecha_respuesta = NOW() WHERE id = ?"
            );
            stmt.setInt(1, tradeId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ctx.json(new ApiResponse(true, "Trueque rechazado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }

            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al rechazar trueque", null));
            e.printStackTrace();
        }
    }

    public void completeTrade(Context ctx) {
        try {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE trueques SET estado_id = 4, fecha_completado = NOW() WHERE id = ?"
            );
            stmt.setInt(1, tradeId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ctx.json(new ApiResponse(true, "Trueque completado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }

            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al completar trueque", null));
            e.printStackTrace();
        }
    }

    public void cancelTrade(Context ctx) {
        try {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE trueques SET estado_id = 5, fecha_respuesta = NOW() WHERE id = ?"
            );
            stmt.setInt(1, tradeId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ctx.json(new ApiResponse(true, "Trueque cancelado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }

            conn.close();
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al cancelar trueque", null));
            e.printStackTrace();
        }
    }

    public void getTradeHistory(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.*, " +
                            "po.nombre as producto_ofrecido_nombre, " +
                            "pd.nombre as producto_deseado_nombre, " +
                            "et.nombre as estado_nombre, " +
                            "CASE WHEN t.usuario_oferente_id = ? THEN 'enviado' ELSE 'recibido' END as tipo " +
                            "FROM trueques t " +
                            "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                            "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                            "JOIN estados_trueque et ON t.estado_id = et.id " +
                            "WHERE t.usuario_oferente_id = ? OR t.usuario_receptor_id = ? " +
                            "ORDER BY t.fecha_propuesta DESC"
            );
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
            conn.close();
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