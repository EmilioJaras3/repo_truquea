package com.trukea;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.trukea.config.DatabaseConfig;
import com.trukea.controllers.AuthController;
import com.trukea.controllers.UserController;
import com.trukea.controllers.ProductController;
import com.trukea.controller.CategoryController;
import com.trukea.controller.CityController;
import com.trukea.controller.TradeController;
import com.trukea.controller.RatingController;
import com.trukea.controllers.ImageController;

public class TrukeaApp {
    public static void main(String[] args) {
        // Inicializar base de datos
        DatabaseConfig.initialize();

        // ✅ Puerto dinámico para despliegue en la nube
        int port = Integer.parseInt(System.getenv("PORT") != null ? System.getenv("PORT") : "3000");

        // Crear aplicación Javalin (versión simplificada compatible)
        Javalin app = Javalin.create(config -> {
            // Configurar CORS de manera simple
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });

            // Servir archivos estáticos (versión compatible)
        }).start(port); // ✅ Usar puerto dinámico

        // Ruta de prueba
        app.get("/api/test", ctx -> {
            ctx.contentType("application/json");
            ctx.json(new ApiResponse(true, "API funcionando correctamente", null));
        });

        // Configurar rutas
        setupRoutes(app);

        System.out.println("🚀 Servidor Trukea API corriendo en puerto " + port);
        System.out.println("🧪 Test: http://localhost:" + port + "/api/test");
        System.out.println("☁️ Cloudinary: do4nedzix");
    }

    private static void setupRoutes(Javalin app) {
        // Controladores
        AuthController authController = new AuthController();
        UserController userController = new UserController();
        ProductController productController = new ProductController();
        CategoryController categoryController = new CategoryController();
        CityController cityController = new CityController();
        TradeController tradeController = new TradeController();
        RatingController ratingController = new RatingController();
        ImageController imageController = new ImageController();

        // Rutas de autenticación
        app.post("/api/auth/register", authController::register);
        app.post("/api/auth/login", authController::login);

        // Rutas de usuarios
        app.get("/api/users/profile/{id}", userController::getProfile);
        app.put("/api/users/profile/{id}", userController::updateProfile);
        app.post("/api/users/complete-registration", userController::completeRegistration);

        // Rutas de productos
        app.get("/api/products", productController::getAllProducts);
        app.get("/api/products/user/{userId}", productController::getUserProducts);
        app.get("/api/products/{id}", productController::getProduct);
        app.post("/api/products", productController::createProduct);
        app.put("/api/products/{id}", productController::updateProduct);
        app.delete("/api/products/{id}", productController::deleteProduct);

        // Rutas de categorías
        app.get("/api/categories", categoryController::getAllCategories);

        // Rutas de ciudades
        app.get("/api/cities", cityController::getAllCities);

        // Rutas de trueques
        app.post("/api/trades/propose", tradeController::proposeTrade);
        app.get("/api/trades/received/{userId}", tradeController::getReceivedRequests);
        app.get("/api/trades/sent/{userId}", tradeController::getSentRequests);
        app.put("/api/trades/accept/{id}", tradeController::acceptTrade);
        app.put("/api/trades/reject/{id}", tradeController::rejectTrade);
        app.put("/api/trades/complete/{id}", tradeController::completeTrade);
        app.put("/api/trades/cancel/{id}", tradeController::cancelTrade);
        app.get("/api/trades/history/{userId}", tradeController::getTradeHistory);

        // Rutas de calificaciones EXISTENTES
        app.post("/api/ratings", ratingController::createRating);
        app.get("/api/ratings/user/{userId}", ratingController::getUserRatings);
        app.get("/api/ratings/pending/{userId}", ratingController::getPendingRatings);

        // Nuevas rutas de estadísticas y rankings
        app.get("/api/ratings/top-users", ratingController::getTopUsers);
        app.get("/api/ratings/most-active", ratingController::getMostActiveUsers);
        app.get("/api/ratings/statistics", ratingController::getStatistics);

        // Rutas de imágenes
        app.get("/api/images/{fileName}", imageController::getImage);
        app.get("/api/images/optimized/{fileName}", imageController::getImageOptimized);
    }
}