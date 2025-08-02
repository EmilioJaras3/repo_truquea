package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import com.trukea.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre, cal.nombre as calidad_nombre, " +
                "u.nombre as usuario_nombre, u.apellido as usuario_apellido, " +
                "ci.nombre as ciudad_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "LEFT JOIN calidades cal ON p.calidad_id = cal.id " +
                "LEFT JOIN usuarios u ON p.usuario_id = u.id " +
                "LEFT JOIN ciudades ci ON u.ciudad_id = ci.id " +
                "WHERE p.disponible = true ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> findByFilters(String categoria, String ciudad, Integer usuarioId) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.nombre as categoria_nombre, cal.nombre as calidad_nombre, " +
                        "u.nombre as usuario_nombre, u.apellido as usuario_apellido, " +
                        "ci.nombre as ciudad_nombre " +
                        "FROM productos p " +
                        "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                        "LEFT JOIN calidades cal ON p.calidad_id = cal.id " +
                        "LEFT JOIN usuarios u ON p.usuario_id = u.id " +
                        "LEFT JOIN ciudades ci ON u.ciudad_id = ci.id " +
                        "WHERE p.disponible = true"
        );

        List<Object> params = new ArrayList<>();
        if (categoria != null && !categoria.isEmpty()) {
            sql.append(" AND c.nombre = ?");
            params.add(categoria);
        }
        if (ciudad != null && !ciudad.isEmpty()) {
            sql.append(" AND ci.nombre = ?");
            params.add(ciudad);
        }
        if (usuarioId != null) {
            sql.append(" AND p.usuario_id != ?");
            params.add(usuarioId);
        }
        sql.append(" ORDER BY p.created_at DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> findByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, cal.nombre as calidad_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "LEFT JOIN calidades cal ON p.calidad_id = cal.id " +
                "WHERE p.usuario_id = ? ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product findById(int id) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, cal.nombre as calidad_nombre, " +
                "u.nombre as usuario_nombre, u.apellido as usuario_apellido, " +
                "ci.nombre as ciudad_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "LEFT JOIN calidades cal ON p.calidad_id = cal.id " +
                "LEFT JOIN usuarios u ON p.usuario_id = u.id " +
                "LEFT JOIN ciudades ci ON u.ciudad_id = ci.id " +
                "WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int save(Product product) {
        String sql = "INSERT INTO productos (nombre, descripcion, valor_estimado, imagen, categoria_id, calidad_id, usuario_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getNombre());
            stmt.setString(2, product.getDescripcion());
            stmt.setDouble(3, product.getValorEstimado());
            stmt.setString(4, product.getImagen());
            stmt.setObject(5, product.getCategoriaId() != 0 ? product.getCategoriaId() : null);
            stmt.setObject(6, product.getCalidadId() != 0 ? product.getCalidadId() : null);
            stmt.setInt(7, product.getUsuarioId());

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

    public boolean update(int id, Product product) {
        StringBuilder sql = new StringBuilder("UPDATE productos SET ");
        List<Object> params = new ArrayList<>();
        boolean hasFields = false;

        if (product.getNombre() != null && !product.getNombre().isEmpty()) {
            sql.append("nombre = ?, ");
            params.add(product.getNombre());
            hasFields = true;
        }
        if (product.getDescripcion() != null && !product.getDescripcion().isEmpty()) {
            sql.append("descripcion = ?, ");
            params.add(product.getDescripcion());
            hasFields = true;
        }
        if (product.getValorEstimado() > 0) {
            sql.append("valor_estimado = ?, ");
            params.add(product.getValorEstimado());
            hasFields = true;
        }
        if (product.getCategoriaId() != 0) {
            sql.append("categoria_id = ?, ");
            params.add(product.getCategoriaId());
            hasFields = true;
        }
        if (product.getCalidadId() != 0) {
            sql.append("calidad_id = ?, ");
            params.add(product.getCalidadId());
            hasFields = true;
        }
        if (product.getImagen() != null && !product.getImagen().isEmpty()) {
            sql.append("imagen = ?, ");
            params.add(product.getImagen());
            hasFields = true;
        }

        if (!hasFields) {
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
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteById(int id) {
        String checkSql = "SELECT COUNT(*) as count FROM trueques WHERE (producto_ofrecido_id = ? OR producto_deseado_id = ?) AND estado_id IN (1, 2)";
        String deleteSql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {


            checkStmt.setInt(1, id);
            checkStmt.setInt(2, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {

            }
        } catch (SQLException e) {
            System.err.println(" Error eliminando producto ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setNombre(rs.getString("nombre"));
        product.setDescripcion(rs.getString("descripcion"));
        product.setValorEstimado(rs.getDouble("valor_estimado"));
        product.setImagen(rs.getString("imagen"));
        product.setCategoriaId(rs.getInt("categoria_id"));
        product.setCalidadId(rs.getInt("calidad_id"));
        product.setUsuarioId(rs.getInt("usuario_id"));
        product.setDisponible(rs.getBoolean("disponible"));

        // Campos adicionales
        product.setCategoriaNombre(rs.getString("categoria_nombre"));
        product.setCalidadNombre(rs.getString("calidad_nombre"));
        product.setUsuarioNombre(rs.getString("usuario_nombre"));
        product.setUsuarioApellido(rs.getString("usuario_apellido"));
        product.setCiudadNombre(rs.getString("ciudad_nombre"));

        return product;
    }
}