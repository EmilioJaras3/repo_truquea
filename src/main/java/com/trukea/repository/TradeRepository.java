package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.Trade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TradeRepository {

    public int save(Trade trade) {
        String sql = "INSERT INTO trueques (producto_ofrecido_id, producto_deseado_id, usuario_oferente_id, usuario_receptor_id, comentario) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, trade.getProductoOfrecidoId());
            stmt.setInt(2, trade.getProductoDeseadoId());
            stmt.setInt(3, trade.getUsuarioOferenteId());
            stmt.setInt(4, trade.getUsuarioReceptorId());
            stmt.setString(5, trade.getComentario());
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

    public List<Trade> findReceivedByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT t.*, po.nombre as producto_ofrecido_nombre, po.imagen as producto_ofrecido_imagen, pd.nombre as producto_deseado_nombre, pd.imagen as producto_deseado_imagen, u.nombre as oferente_nombre, u.apellido as oferente_apellido, et.nombre as estado_nombre FROM trueques t JOIN productos po ON t.producto_ofrecido_id = po.id JOIN productos pd ON t.producto_deseado_id = pd.id JOIN usuarios u ON t.usuario_oferente_id = u.id JOIN estados_trueque et ON t.estado_id = et.id WHERE t.usuario_receptor_id = ? ORDER BY t.fecha_propuesta DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    public List<Trade> findSentByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT t.*, po.nombre as producto_ofrecido_nombre, po.imagen as producto_ofrecido_imagen, pd.nombre as producto_deseado_nombre, pd.imagen as producto_deseado_imagen, receptor.nombre as receptor_nombre, receptor.apellido as receptor_apellido, et.nombre as estado_nombre FROM trueques t JOIN productos po ON t.producto_ofrecido_id = po.id JOIN productos pd ON t.producto_deseado_id = pd.id JOIN usuarios receptor ON t.usuario_receptor_id = receptor.id JOIN estados_trueque et ON t.estado_id = et.id WHERE t.usuario_oferente_id = ? ORDER BY t.fecha_propuesta DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Trade trade = new Trade();
                trade.setId(rs.getInt("id"));
                trade.setProductoOfrecidoId(rs.getInt("producto_ofrecido_id"));
                trade.setProductoDeseadoId(rs.getInt("producto_deseado_id"));
                trade.setUsuarioOferenteId(rs.getInt("usuario_oferente_id"));
                trade.setUsuarioReceptorId(rs.getInt("usuario_receptor_id"));
                trade.setComentario(rs.getString("comentario"));
                trade.setEstadoId(rs.getInt("estado_id"));
                trade.setProductoOfrecidoNombre(rs.getString("producto_ofrecido_nombre"));
                trade.setProductoOfrecidoImagen(rs.getString("producto_ofrecido_imagen"));
                trade.setProductoDeseadoNombre(rs.getString("producto_deseado_nombre"));
                trade.setProductoDeseadoImagen(rs.getString("producto_deseado_imagen"));
                trade.setReceptorNombre(rs.getString("receptor_nombre"));
                trade.setReceptorApellido(rs.getString("receptor_apellido"));
                trade.setEstadoNombre(rs.getString("estado_nombre"));
                trades.add(trade);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    public boolean updateStatus(int tradeId, int statusId) {
        String sql = "UPDATE trueques SET estado_id = ?, fecha_respuesta = NOW() WHERE id = ?";
        if (statusId == 4) { // Completado
            sql = "UPDATE trueques SET estado_id = ?, fecha_completado = NOW() WHERE id = ?";
        }
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, statusId);
            stmt.setInt(2, tradeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getOwnerIdByProductId(int productId) {
        String sql = "SELECT usuario_id FROM productos WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("usuario_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Trade> findHistoryByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT t.*, po.nombre as producto_ofrecido_nombre, pd.nombre as producto_deseado_nombre, et.nombre as estado_nombre FROM trueques t JOIN productos po ON t.producto_ofrecido_id = po.id JOIN productos pd ON t.producto_deseado_id = pd.id JOIN estados_trueque et ON t.estado_id = et.id WHERE t.usuario_oferente_id = ? OR t.usuario_receptor_id = ? ORDER BY t.fecha_propuesta DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
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