package com.trukea.service;

import io.javalin.http.UploadedFile;
import com.trukea.config.CloudinaryConfig;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageService {
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Sube una imagen a Cloudinary
     * @param uploadedFile Archivo subido desde el frontend
     * @param folder Carpeta en Cloudinary ("productos" o "usuarios")
     * @return URL de la imagen subida o null si falla
     */
    public String uploadImage(UploadedFile uploadedFile, String folder) {
        try {
            // ✅ Validar archivo
            if (!isValidImage(uploadedFile)) {
                System.out.println("❌ Archivo inválido: " + uploadedFile.filename());
                return null;
            }

            // ✅ Convertir a bytes
            byte[] imageBytes = uploadedFile.content().readAllBytes();

            // ✅ Subir usando nuestro CloudinaryConfig personalizado
            String imageUrl = CloudinaryConfig.uploadImage(imageBytes, folder);

            if (imageUrl != null) {
                System.out.println("✅ Imagen subida exitosamente: " + imageUrl);
            }

            return imageUrl;

        } catch (IOException e) {
            System.err.println("❌ Error leyendo archivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina una imagen de Cloudinary
     * @param imageUrl URL completa de la imagen a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteImage(String imageUrl) {
        return CloudinaryConfig.deleteImage(imageUrl);
    }

    /**
     * Valida si el archivo es una imagen válida
     */
    private boolean isValidImage(UploadedFile file) {
        if (file == null) {
            return false;
        }

        // Verificar tamaño (máximo 5MB)
        if (file.size() > MAX_FILE_SIZE) {
            System.out.println("❌ Archivo muy grande: " + file.size() + " bytes (máximo 5MB)");
            return false;
        }

        // Verificar extensión
        String filename = file.filename().toLowerCase();
        if (!filename.contains(".")) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        boolean isValid = ALLOWED_FORMATS.contains(extension);

        if (!isValid) {
            System.out.println("❌ Formato no permitido: " + extension + " (permitidos: jpg, png, gif, webp)");
        }

        return isValid;
    }
}