package com.trukea.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import com.trukea.ApiResponse;
import com.trukea.service.UserService;
import com.trukea.service.ImageService;  // ✅ NUEVO IMPORT
import com.trukea.dto.UserDTO;
import com.trukea.models.User;
import java.util.Map;

public class UserController {
    private UserService userService;
    private ImageService imageService;

    public UserController() {
        this.userService = new UserService();
        this.imageService = new ImageService();  // ✅ INICIALIZAR CLOUDINARY SERVICE
    }

    public void getProfile(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            UserDTO user = userService.getUserById(userId);

            if (user != null) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Perfil obtenido exitosamente", Map.of("user", user)));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Usuario no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    // ✅ MÉTODO UPDATEPROFILE ACTUALIZADO CON CLOUDINARY
    public void updateProfile(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));

            // ✅ Obtener usuario actual para manejar imagen existente
            UserDTO currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Usuario no encontrado", null));
                return;
            }

            User user = new User();
            String nombre = ctx.formParam("nombre");
            String apellido = ctx.formParam("apellido");
            String ciudad = ctx.formParam("ciudad");
            String fechaNacimiento = ctx.formParam("fechaNacimiento");

            if (nombre != null && !nombre.isEmpty()) {
                user.setNombre(nombre);
            }
            if (apellido != null && !apellido.isEmpty()) {
                user.setApellido(apellido);
            }
            if (ciudad != null && !ciudad.isEmpty()) {
                user.setCiudadId(Integer.parseInt(ciudad));
            }
            if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                user.setFechaNacimiento(java.time.LocalDate.parse(fechaNacimiento));
            }

            // ✅ MANEJAR IMAGEN DE PERFIL CON CLOUDINARY
            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile != null) {
                System.out.println("📤 Actualizando imagen de perfil para usuario ID: " + userId);

                // Subir nueva imagen de perfil
                String newImageUrl = imageService.uploadImage(uploadedFile, "usuarios");

                if (newImageUrl != null) {
                    // Eliminar imagen anterior si existe y es de Cloudinary
                    if (currentUser.getImagenPerfil() != null && currentUser.getImagenPerfil().contains("cloudinary.com")) {
                        boolean deleted = imageService.deleteImage(currentUser.getImagenPerfil());
                        if (deleted) {
                            System.out.println("🗑️ Imagen de perfil anterior eliminada para usuario ID: " + userId);
                        }
                    }

                    user.setImagenPerfil(newImageUrl);
                    System.out.println("✅ Nueva imagen de perfil actualizada para usuario ID: " + userId);
                } else {
                    ctx.contentType("application/json");
                    ctx.status(400).json(new ApiResponse(false, "Error al subir la imagen de perfil. Verifica el formato y tamaño.", null));
                    return;
                }
            }

            boolean success = userService.updateUser(userId, user);
            if (success) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Perfil actualizado exitosamente",
                        Map.of("imageUrl", user.getImagenPerfil() != null ? user.getImagenPerfil() :
                                (currentUser.getImagenPerfil() != null ? currentUser.getImagenPerfil() : ""))));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Usuario no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar perfil", null));
            e.printStackTrace();
        }
    }

    public void completeRegistration(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int userId = ((Double) body.get("userId")).intValue();
            String nombre = (String) body.get("nombre");
            int ciudad = ((Double) body.get("ciudad")).intValue();

            if (nombre == null || nombre.isEmpty()) {
                ctx.contentType("application/json");
                ctx.status(400).json(new ApiResponse(false, "Nombre es obligatorio", null));
                return;
            }

            User user = new User();
            user.setNombre(nombre);
            user.setCiudadId(ciudad);

            boolean success = userService.updateUser(userId, user);
            if (success) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Registro completado exitosamente", null));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Usuario no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al completar registro", null));
            e.printStackTrace();
        }
    }
}