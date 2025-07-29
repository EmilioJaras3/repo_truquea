package com.trukea.controllers;

import io.javalin.http.Context;
import java.nio.file.Files;
import java.nio.file.Paths;
public class ImageController {


    public void getImage(Context ctx) {
        try {
            String fileName = ctx.pathParam("fileName");
            byte[] imageBytes = Files.readAllBytes(Paths.get("uploads", fileName));
            ctx.result(imageBytes);
        } catch (Exception e) {
            ctx.status(404).result("Imagen no encontrada");
        }
    }
}