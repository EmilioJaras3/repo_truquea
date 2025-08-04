package com.trukea.repository;

import com.trukea.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CityRepository {

    public List<Map<String, Object>> findAll() {
        List<Map<String, Object>> cities = new ArrayList<>();
        String sql = "SELECT * FROM ciudades ORDER BY nombre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> city = new HashMap<>();
                city.put("id", rs.getInt("id"));
                city.put("nombre", rs.getString("nombre"));
                cities.add(city);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }
}