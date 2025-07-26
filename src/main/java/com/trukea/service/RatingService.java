package com.trukea.service;

import com.trukea.models.Rating;
import com.trukea.repository.RatingRepository;

import java.util.List;

public class RatingService {
    private RatingRepository ratingRepository;
    private UserService userService;

    public RatingService() {
        this.ratingRepository = new RatingRepository();
        this.userService = new UserService();
    }

    public boolean hasUserRatedTrade(int tradeId, int userId) {
        return ratingRepository.existsByTradeAndUser(tradeId, userId);
    }

    public int createRating(int tradeId, int calificadorId, int calificadoId, int puntuacion, String comentario) {
        Rating rating = new Rating();
        rating.setTruequeId(tradeId);
        rating.setCalificadorId(calificadorId);
        rating.setCalificadoId(calificadoId);
        rating.setPuntuacion(puntuacion);
        rating.setComentario(comentario);

        int ratingId = ratingRepository.save(rating);

        if (ratingId > 0) {
            // Actualizar promedio de calificación del usuario
            userService.updateUserRating(calificadoId);
        }

        return ratingId;
    }

    public List<Rating> getUserRatings(int userId) {
        return ratingRepository.findByUserId(userId);
    }
}
