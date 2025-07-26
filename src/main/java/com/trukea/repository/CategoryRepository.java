package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CategoryRepository {

    public List<Map<String, Object>> findAll() {
        List<Map<String, Object>> categories = new ArrayList<>();
        try {
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categorias ORDER BY nombre");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("nombre", rs.getString("nombre"));
                categories.add(category);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}