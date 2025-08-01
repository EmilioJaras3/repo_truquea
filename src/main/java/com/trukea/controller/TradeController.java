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
            //  LOG: Inicio del proceso
            System.out.println(" === INICIANDO PROPUESTA DE TRUEQUE ===");

            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            System.out.println(" Body recibido: " + body);

            //  LOG: Extrayendo datos del body
            int productoOfrecidoId = getIntegerFromBody(body, "producto_ofrecido_id");
            int productoDeseadoId = getIntegerFromBody(body, "producto_deseado_id");
            int usuarioOferenteId = getIntegerFromBody(body, "usuario_oferente_id");
            String comentario = (String) body.get("comentario");

            System.out.println(" Datos extraídos:");
            System.out.println("   - Producto Ofrecido ID: " + productoOfrecidoId);
            System.out.println("   - Producto Deseado ID: " + productoDeseadoId);
            System.out.println("   - Usuario Oferente ID: " + usuarioOferenteId);
            System.out.println("   - Comentario: " + comentario);

            //  LOG: Validaciones
            if (productoOfrecidoId <= 0 || productoDeseadoId <= 0 || usuarioOferenteId <= 0) {
                System.out.println(" Error: IDs inválidos");
                ctx.status(400).json(new ApiResponse(false, "IDs de productos y usuario son obligatorios", null));
                return;
            }

            System.out.println(" Validaciones básicas pasadas");

            System.out.println("🔌 Conectando a base de datos...");
            Connection conn = DatabaseConfig.getConnection();
            System.out.println(" Conexión establecida");

            System.out.println("🔍 Buscando dueño del producto ID " + productoDeseadoId + "...");
            PreparedStatement getOwnerStmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?");
            getOwnerStmt.setInt(1, productoDeseadoId);
            ResultSet rs = getOwnerStmt.executeQuery();

            if (!rs.next()) {
                System.out.println(" Error: Producto deseado ID " + productoDeseadoId + " no encontrado");
                ctx.status(400).json(new ApiResponse(false, "Producto deseado no encontrado", null));
                conn.close();
                return;
            }

            int usuarioReceptorId = rs.getInt("usuario_id");
            System.out.println(" Producto deseado encontrado. Dueño: Usuario ID " + usuarioReceptorId);

            if (usuarioOferenteId == usuarioReceptorId) {
                System.out.println(" Error: Usuario " + usuarioOferenteId + " intenta intercambiar consigo mismo");
                ctx.status(400).json(new ApiResponse(false, "No puedes intercambiar con tus propios productos", null));
                conn.close();
                return;
            }

            System.out.println("Validación de auto-intercambio pasada");

            System.out.println("🔍 Verificando que el producto ofrecido ID " + productoOfrecidoId + " pertenece al usuario " + usuarioOferenteId + "...");
            PreparedStatement checkOwnerStmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?");
            checkOwnerStmt.setInt(1, productoOfrecidoId);
            ResultSet ownerRs = checkOwnerStmt.executeQuery();

            if (!ownerRs.next()) {
                System.out.println(" Error: Producto ofrecido ID " + productoOfrecidoId + " no encontrado");
                ctx.status(400).json(new ApiResponse(false, "Producto ofrecido no encontrado", null));
                conn.close();
                return;
            }

            int realOwnerOfOfferedProduct = ownerRs.getInt("usuario_id");
            System.out.println(" Producto ofrecido ID " + productoOfrecidoId + " pertenece al usuario ID " + realOwnerOfOfferedProduct);

            if (realOwnerOfOfferedProduct != usuarioOferenteId) {
                System.out.println(" Error: Usuario " + usuarioOferenteId + " no es dueño del producto " + productoOfrecidoId);
                System.out.println("   El verdadero dueño es: Usuario ID " + realOwnerOfOfferedProduct);
                ctx.status(400).json(new ApiResponse(false, "No puedes ofrecer un producto que no te pertenece", null));
                conn.close();
                return;
            }

            System.out.println(" Verificación de propiedad pasada");

            //  LOG: Insertar propuesta de trueque
            System.out.println(" Insertando propuesta de trueque en base de datos...");
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

            System.out.println(" Parámetros SQL:");
            System.out.println("   - producto_ofrecido_id: " + productoOfrecidoId);
            System.out.println("   - producto_deseado_id: " + productoDeseadoId);
            System.out.println("   - usuario_oferente_id: " + usuarioOferenteId);
            System.out.println("   - usuario_receptor_id: " + usuarioReceptorId);
            System.out.println("   - comentario: " + comentario);

            int affectedRows = insertStmt.executeUpdate();
            System.out.println(" Filas afectadas: " + affectedRows);

            if (affectedRows > 0) {
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int tradeId = generatedKeys.getInt(1);
                    System.out.println("🎉 ¡TRUEQUE CREADO EXITOSAMENTE!");
                    System.out.println("   - Trade ID: " + tradeId);
                    System.out.println("   - Usuario " + usuarioOferenteId + " ofrece producto " + productoOfrecidoId);
                    System.out.println("   - A usuario " + usuarioReceptorId + " por producto " + productoDeseadoId);

                    ctx.status(201).json(new ApiResponse(true, "Propuesta de trueque enviada exitosamente",
                            Map.of("tradeId", tradeId)));
                }
            } else {
                System.out.println(" Error: No se insertaron filas");
                ctx.status(500).json(new ApiResponse(false, "Error al insertar trueque", null));
            }

            conn.close();
            System.out.println("=== FIN PROCESO TRUEQUE ===");

        } catch (SQLException e) {
            System.err.println(" ERROR SQL:");
            System.err.println("   - Mensaje: " + e.getMessage());
            System.err.println("   - Código: " + e.getErrorCode());
            System.err.println("   - Estado SQL: " + e.getSQLState());
            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error de base de datos: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println(" ERROR GENERAL:");
            System.err.println("   - Tipo: " + e.getClass().getSimpleName());
            System.err.println("   - Mensaje: " + e.getMessage());
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