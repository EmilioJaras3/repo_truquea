package com.trukea.controller;

import io.javalin.http.Context;
import com.trukea.ApiResponse;
import com.trukea.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityController {

    public void getAllCities(Context ctx) {
        String sql = "SELECT * FROM ciudades ORDER BY nombre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Map<String, Object>> cities = new ArrayList<>();
            while (rs.next()) {
                cities.add(Map.of(
                        "id", rs.getInt("id"),
                        "nombre", rs.getString("nombre")
                ));
            }
            ctx.json(new ApiResponse(true, "Ciudades obtenidas", Map.of("cities", cities)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }
}