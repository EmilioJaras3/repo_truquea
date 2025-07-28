package com.trukea.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import com.trukea.ApiResponse;
import com.trukea.service.UserService;
import com.trukea.dto.UserDTO;
import com.trukea.models.User;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserController {
    private UserService userService;

    public UserController() {
        this.userService = new UserService();
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

    public void updateProfile(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));

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

            // Manejar imagen
            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile != null) {
                String imageName = saveUploadedFile(uploadedFile);
                if (imageName != null) {
                    user.setImagenPerfil(imageName);
                }
            }

            boolean success = userService.updateUser(userId, user);
            if (success) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Perfil actualizado exitosamente", null));
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

    private String saveUploadedFile(UploadedFile uploadedFile) {
        try {
            String fileName = System.currentTimeMillis() + "_" + uploadedFile.filename();
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(uploadedFile.content(), filePath);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}