package com.trukea.controller;

import io.javalin.http.Context;
import com.trukea.ApiResponse;
import com.trukea.service.RatingService;
import com.trukea.models.Rating;
import java.util.List;
import java.util.Map;

public class RatingController {
    private RatingService ratingService;

    public RatingController() {
        this.ratingService = new RatingService();
    }

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
}