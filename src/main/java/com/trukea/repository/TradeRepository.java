package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.Trade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TradeRepository {

    public int save(Trade trade) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO trueques (producto_ofrecido_id, producto_deseado_id, usuario_oferente_id, usuario_receptor_id, comentario) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setInt(1, trade.getProductoOfrecidoId());
            stmt.setInt(2, trade.getProductoDeseadoId());
            stmt.setInt(3, trade.getUsuarioOferenteId());
            stmt.setInt(4, trade.getUsuarioReceptorId());
            stmt.setString(5, trade.getComentario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int tradeId = generatedKeys.getInt(1);
                    conn.close();
                    return tradeId;
                }
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Trade> findReceivedByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        try {
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
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    public List<Trade> findSentByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        try {
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
            while (rs.next()) {
                Trade trade = mapResultSetToTrade(rs);
                trade.setReceptorNombre(rs.getString("receptor_nombre"));
                trade.setReceptorApellido(rs.getString("receptor_apellido"));
                trades.add(trade);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    public boolean updateStatus(int tradeId, int statusId) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            String sql = "UPDATE trueques SET estado_id = ?, fecha_respuesta = NOW() WHERE id = ?";
            if (statusId == 4) { // Completado
                sql = "UPDATE trueques SET estado_id = ?, fecha_completado = NOW() WHERE id = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, statusId);
            stmt.setInt(2, tradeId);

            int affectedRows = stmt.executeUpdate();
            conn.close();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getOwnerIdByProductId(int productId) {
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT usuario_id FROM productos WHERE id = ?");
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int ownerId = rs.getInt("usuario_id");
                conn.close();
                return ownerId;
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Trade> findHistoryByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.*, " +
                            "po.nombre as producto_ofrecido_nombre, " +
                            "pd.nombre as producto_deseado_nombre, " +
                            "et.nombre as estado_nombre " +
                            "FROM trueques t " +
                            "JOIN productos po ON t.producto_ofrecido_id = po.id " +
                            "JOIN productos pd ON t.producto_deseado_id = pd.id " +
                            "JOIN estados_trueque et ON t.estado_id = et.id " +
                            "WHERE t.usuario_oferente_id = ? OR t.usuario_receptor_id = ? " +
                            "ORDER BY t.fecha_propuesta DESC"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
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