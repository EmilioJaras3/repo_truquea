package com.trukea.routes;

import io.javalin.Javalin;
import com.trukea.controllers.AuthController;
import com.trukea.controller.CityController;
import com.trukea.controller.RatingController;

public class UserRoutes {

    public static void configure(Javalin app) {
        AuthController authController = new AuthController();
        CityController cityController = new CityController();
        RatingController ratingController = new RatingController();

        // Rutas de autenticación
        app.post("/api/auth/register", authController::register);
        app.post("/api/auth/login", authController::login);

        // Rutas de ciudades
        app.get("/api/cities", cityController::getAllCities);

        // Rutas de calificaciones
        app.post("/api/ratings", ratingController::createRating);
        app.get("/api/ratings/user/{userId}", ratingController::getUserRatings);
        app.get("/api/ratings/pending/{userId}", ratingController::getPendingRatings);
    }
}
