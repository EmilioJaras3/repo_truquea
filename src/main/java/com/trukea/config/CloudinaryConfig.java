package com.trukea.config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudinaryConfig {
    private static final String CLOUD_NAME = "do4nedzix";
    private static final String API_KEY = "386477793112854";
    private static final String API_SECRET = "KporUuSxHWKFRqBCw-7FVZw63oA";


    public static String uploadImage(byte[] imageBytes, String folder) {
        try {
            System.out.println("🌩️ Conectando a Cloudinary (modo simple)...");

            String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            String boundary = "----CloudinaryBoundary" + System.currentTimeMillis();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream out = connection.getOutputStream()) {
                long timestamp = System.currentTimeMillis() / 1000;
                String folderPath = "trukea/" + folder;

                // ✅ SOLO PARÁMETROS BÁSICOS
                writeFormField(out, boundary, "api_key", API_KEY);
                writeFormField(out, boundary, "timestamp", String.valueOf(timestamp));
                writeFormField(out, boundary, "folder", folderPath);

                String signature = createSimpleSignature(timestamp, folderPath);
                writeFormField(out, boundary, "signature", signature);

                // Archivo
                writeFileField(out, boundary, "file", "image.jpg", imageBytes);

                // Cerrar boundary
                out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }

            int responseCode = connection.getResponseCode();
            System.out.println("🌩️ Cloudinary respuesta: " + responseCode);

            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream())
                );
                String response = reader.lines().collect(java.util.stream.Collectors.joining());
                reader.close();

                System.out.println("📋 Respuesta exitosa de Cloudinary");

                if (response.contains("secure_url")) {
                    int start = response.indexOf("\"secure_url\":\"") + 14;
                    int end = response.indexOf("\"", start);
                    String imageUrl = response.substring(start, end);

                    System.out.println("✅ Imagen subida: " + imageUrl);
                    return imageUrl;
                }
            } else {
                // Leer error
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream())
                );
                String errorResponse = errorReader.lines().collect(java.util.stream.Collectors.joining());
                errorReader.close();

                System.err.println(" Error Cloudinary código: " + responseCode);
                System.err.println(" Error detalle: " + errorResponse);
            }

        } catch (IOException e) {
            System.err.println(" Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    private static String createSimpleSignature(long timestamp, String folder) {
        try {
            // Solo parámetros básicos en orden alfabético
            String params = "folder=" + folder + "&timestamp=" + timestamp + API_SECRET;

            System.out.println("🔐 Creando firma para: folder=" + folder + "&timestamp=" + timestamp);

            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(params.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String signature = hexString.toString();
            System.out.println("🔐 Firma generada: " + signature);

            return signature;

        } catch (Exception e) {
            System.err.println(" Error creando firma: " + e.getMessage());
            return "";
        }
    }


    // * Elimina una imagen de Cloudinary

    public static boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                return false;
            }

            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                return false;
            }

            String deleteUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/destroy";

            URL url = new URL(deleteUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            long timestamp = System.currentTimeMillis() / 1000;
            String deleteParams = "public_id=" + publicId + "&timestamp=" + timestamp + API_SECRET;

            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(deleteParams.getBytes(StandardCharsets.UTF_8));

            StringBuilder signature = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) signature.append('0');
                signature.append(hex);
            }

            String postData = "public_id=" + java.net.URLEncoder.encode(publicId, "UTF-8") +
                    "&api_key=" + API_KEY +
                    "&timestamp=" + timestamp +
                    "&signature=" + signature.toString();

            try (OutputStream out = connection.getOutputStream()) {
                out.write(postData.getBytes());
            }

            int responseCode = connection.getResponseCode();
            boolean success = responseCode == 200;

            if (success) {
                System.out.println("✅ Imagen eliminada: " + publicId);
            }

            return success;

        } catch (Exception e) {
            System.err.println(" Error eliminando imagen: " + e.getMessage());
            return false;
        }
    }

    // Métodos auxiliares
    private static void writeFormField(OutputStream out, String boundary, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        out.write((value + "\r\n").getBytes());
    }

    private static void writeFileField(OutputStream out, String boundary, String name, String filename, byte[] data) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n").getBytes());
        out.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
        out.write(data);
    }

    private static String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/");

            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i]) && i + 2 < parts.length) {
                    StringBuilder publicId = new StringBuilder();

                    int startIndex = i + 1;
                    if (parts[startIndex].startsWith("v") && parts[startIndex].matches("v\\d+")) {
                        startIndex = i + 2;
                    }

                    for (int j = startIndex; j < parts.length; j++) {
                        if (j > startIndex) publicId.append("/");
                        publicId.append(parts[j]);
                    }

                    String result = publicId.toString();
                    if (result.contains(".")) {
                        result = result.substring(0, result.lastIndexOf("."));
                    }

                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error extrayendo public_id: " + e.getMessage());
        }

        return null;
    }
}