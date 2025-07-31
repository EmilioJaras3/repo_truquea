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
        DatabaseConfig.initialize();

        int port = Integer.parseInt(System.getenv("PORT") != null ? System.getenv("PORT") : "3000");

        // Crear aplicación Javalin con CORS mejorado
        Javalin app = Javalin.create(config -> {
            // ✅ CORS MEJORADO - Configuración más específica
            config.plugins.enableCors(cors -> {
                cors.add(corsConfig -> {
                    corsConfig.anyHost();
                    corsConfig.allowCredentials = false;
                    corsConfig.exposeHeader("Access-Control-Allow-Origin");
                });
            });

            // ✅ AGREGAR archivos estáticos si necesitas
            // config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(port);

        // ✅ CONFIGURAR HEADERS CORS MANUALMENTE PARA TODOS LOS REQUESTS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            ctx.header("Access-Control-Max-Age", "3600");
        });

        // ✅ MANEJAR PREFLIGHT OPTIONS REQUESTS
        app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            ctx.status(200);
        });

        // Ruta de prueba
        app.get("/api/test", ctx -> {
            ctx.contentType("application/json");
            ctx.json(new ApiResponse(true, "API funcionando correctamente", null));
        });

        // Configurar rutas
        setupRoutes(app);

        System.out.println("🚀 Servidor Trukea API corriendo en puerto " + port);
        System.out.println("🔗 Test: http://localhost:" + port + "/api/test");
        System.out.println("☁️ Cloudinary: do4nedzix");
        System.out.println("🌐 CORS habilitado para todos los orígenes");
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
        app.get("/api/debug/product/{id}", productController::debugProduct);

        // Rutas de categorías
        app.get("/api/categories", categoryController::getAllCategories);

        // Rutas de ciudades
        app.get("/api/cities", cityController::getAllCities);

        // ✅ RUTAS DE TRUEQUES - Con logging extra para debug
        app.post("/api/trades/propose", ctx -> {
            System.out.println("🔄 Recibida propuesta de trueque desde: " + ctx.header("Origin"));
            System.out.println("📋 Content-Type: " + ctx.header("Content-Type"));
            tradeController.proposeTrade(ctx);
        });

        app.get("/api/trades/received/{userId}", tradeController::getReceivedRequests);
        app.get("/api/trades/sent/{userId}", tradeController::getSentRequests);
        app.put("/api/trades/accept/{id}", tradeController::acceptTrade);
        app.put("/api/trades/reject/{id}", tradeController::rejectTrade);
        app.put("/api/trades/complete/{id}", tradeController::completeTrade);
        app.put("/api/trades/cancel/{id}", tradeController::cancelTrade);
        app.get("/api/trades/history/{userId}", tradeController::getTradeHistory);

        // Rutas de calificaciones
        app.post("/api/ratings", ratingController::createRating);
        app.get("/api/ratings/user/{userId}", ratingController::getUserRatings);
        app.get("/api/ratings/pending/{userId}", ratingController::getPendingRatings);

        // Rutas de estadísticas y rankings
        app.get("/api/ratings/top-users", ratingController::getTopUsers);
        app.get("/api/ratings/most-active", ratingController::getMostActiveUsers);
        app.get("/api/ratings/statistics", ratingController::getStatistics);

        // Rutas de imágenes
        app.get("/api/images/{fileName}", imageController::getImage);
        app.get("/api/images/optimized/{fileName}", imageController::getImageOptimized);
    }
}