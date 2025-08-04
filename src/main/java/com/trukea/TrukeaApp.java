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

        Javalin app = Javalin.create().start(port);

        app.before("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
            ctx.header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With, Access-Control-Request-Method, Access-Control-Request-Headers");
            ctx.header("Access-Control-Allow-Credentials", "false");
            ctx.header("Access-Control-Max-Age", "86400");
            System.out.println("🌐 CORS Headers agregados para: " + ctx.method() + " " + ctx.path());
            System.out.println("🔗 Origin: " + ctx.header("Origin"));
        });

        app.options("*", ctx -> {
            System.out.println("✅ Manejando preflight OPTIONS para: " + ctx.path());
            ctx.status(200);
            ctx.result("");
        });

        app.get("/api/test", ctx -> {
            ctx.contentType("application/json");
            ctx.json(new ApiResponse(true, "API funcionando correctamente", null));
        });

        setupRoutes(app);

        System.out.println("🚀 Servidor Trukea API corriendo en puerto " + port);
        System.out.println("🔗 Test: http://localhost:" + port + "/api/test");
        System.out.println("☁️ Cloudinary: do4nedzix");
        System.out.println("🌐 CORS habilitado para todos los orígenes");
    }

    private static void setupRoutes(Javalin app) {
        AuthController authController = new AuthController();
        UserController userController = new UserController();
        ProductController productController = new ProductController();
        CategoryController categoryController = new CategoryController();
        CityController cityController = new CityController();
        TradeController tradeController = new TradeController();
        RatingController ratingController = new RatingController();
        ImageController imageController = new ImageController();

        app.post("/api/auth/register", authController::register);
        app.post("/api/auth/login", authController::login);

        app.get("/api/users/profile/{id}", userController::getProfile);
        app.put("/api/users/profile/{id}", userController::updateProfile);
        app.post("/api/users/complete-registration", userController::completeRegistration);

        app.get("/api/products", productController::getAllProducts);
        app.get("/api/products/user/{userId}", productController::getUserProducts);
        app.get("/api/products/{id}", productController::getProduct);
        app.post("/api/products", productController::createProduct);
        app.put("/api/products/{id}", productController::updateProduct);
        app.delete("/api/products/{id}", productController::deleteProduct);
        app.get("/api/debug/product/{id}", productController::debugProduct);

        app.get("/api/categories", categoryController::getAllCategories);

        app.get("/api/cities", cityController::getAllCities);

        app.post("/api/trades/propose", ctx -> {
            try {
                System.out.println("🔄 === PROPUESTA DE TRUEQUE ===");
                System.out.println("📨 Método: " + ctx.method());
                System.out.println("🌐 Origin: " + ctx.header("Origin"));
                System.out.println("📋 Content-Type: " + ctx.header("Content-Type"));
                System.out.println("📄 Body: " + ctx.body());
                System.out.println("===============================");

                if ("OPTIONS".equals(ctx.method())) {
                    ctx.status(200);
                    return;
                }

                tradeController.proposeTrade(ctx);

            } catch (Exception e) {
                System.err.println("❌ Error en proposeTrade: " + e.getMessage());
                e.printStackTrace();
                ctx.status(500).json(new ApiResponse(false, "Error interno del servidor: " + e.getMessage(), null));
            }
        });

        app.get("/api/trades/received/{userId}", tradeController::getReceivedRequests);
        app.get("/api/trades/sent/{userId}", tradeController::getSentRequests);
        app.put("/api/trades/accept/{id}", tradeController::acceptTrade);
        app.put("/api/trades/reject/{id}", tradeController::rejectTrade);
        app.put("/api/trades/complete/{id}", tradeController::completeTrade);
        app.put("/api/trades/cancel/{id}", tradeController::cancelTrade);
        app.get("/api/trades/history/{userId}", tradeController::getTradeHistory);

        app.post("/api/ratings", ratingController::createRating);
        app.get("/api/ratings/user/{userId}", ratingController::getUserRatings);
        app.get("/api/ratings/pending/{userId}", ratingController::getPendingRatings);

        app.get("/api/ratings/top-users", ratingController::getTopUsers);
        app.get("/api/ratings/most-active", ratingController::getMostActiveUsers);
        app.get("/api/ratings/statistics", ratingController::getStatistics);

        app.get("/api/images/{fileName}", imageController::getImage);
        app.get("/api/images/optimized/{fileName}", imageController::getImageOptimized);
    }
}