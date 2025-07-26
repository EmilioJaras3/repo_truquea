package com.trukea.routes;

import io.javalin.Javalin;
import com.trukea.controllers.ProductController;
import com.trukea.controller.CategoryController;

public class ProductsRoutes {

    public static void configure(Javalin app) {
        ProductController productController = new ProductController();
        CategoryController categoryController = new CategoryController();

        // Rutas de productos
        app.get("/api/products", productController::getAllProducts);
        app.get("/api/products/user/{userId}", productController::getUserProducts);
        app.get("/api/products/{id}", productController::getProduct);
        app.post("/api/products", productController::createProduct);
        app.put("/api/products/{id}", productController::updateProduct);
        app.delete("/api/products/{id}", productController::deleteProduct);

        // Rutas de categorías
        app.get("/api/categories", categoryController::getAllCategories);
    }
}