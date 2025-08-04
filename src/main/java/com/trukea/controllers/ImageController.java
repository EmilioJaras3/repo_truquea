package com.trukea.controllers;

import io.javalin.http.Context;
import com.trukea.ApiResponse;

public class ImageController {


    public void getImage(Context ctx) {
        try {
            String fileName = ctx.pathParam("fileName");

            // ✅ Si ya es una URL completa de Cloudinary, redirigir directamente
            if (fileName.startsWith("https://res.cloudinary.com")) {
                ctx.redirect(fileName);
                return;
            }

            // ✅ Si es solo el nombre del archivo, construir URL de Cloudinary
            String cloudinaryUrl = "https://res.cloudinary.com/do4nedzix/image/upload/" + fileName;
            ctx.redirect(cloudinaryUrl);

        } catch (Exception e) {
            ctx.status(404).json(new ApiResponse(false, "Imagen no encontrada", null));
            e.printStackTrace();
        }
    }

    /**
     * ✅ Endpoint alternativo para obtener imagen optimizada
     * Permite pasar parámetros de transformación
     */
    public void getImageOptimized(Context ctx) {
        try {
            String fileName = ctx.pathParam("fileName");
            String width = ctx.queryParam("w"); // ?w=300
            String height = ctx.queryParam("h"); // ?h=200
            String quality = ctx.queryParam("q"); // ?q=80

            // ✅ Construir URL con transformaciones
            StringBuilder cloudinaryUrl = new StringBuilder("https://res.cloudinary.com/do4nedzix/image/upload/");

            // Agregar transformaciones si están presentes
            if (width != null || height != null || quality != null) {
                if (width != null) cloudinaryUrl.append("w_").append(width).append(",");
                if (height != null) cloudinaryUrl.append("h_").append(height).append(",");
                if (quality != null) cloudinaryUrl.append("q_").append(quality).append(",");

                // Remover la última coma y agregar crop
                cloudinaryUrl.setLength(cloudinaryUrl.length() - 1);
                cloudinaryUrl.append(",c_fit/");
            }

            cloudinaryUrl.append(fileName);

            ctx.redirect(cloudinaryUrl.toString());

        } catch (Exception e) {
            ctx.status(404).json(new ApiResponse(false, "Imagen no encontrada", null));
            e.printStackTrace();
        }
    }
}