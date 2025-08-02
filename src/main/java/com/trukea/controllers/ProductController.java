package com.trukea.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import com.trukea.ApiResponse;
import com.trukea.service.ProductService;
import com.trukea.service.ImageService;
import com.trukea.dto.ProductDTO;
import com.trukea.dto.CreateProductRequestDTO;
import com.trukea.models.Product;
import com.trukea.config.DatabaseConfig;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProductController {
    private ProductService productService;
    private ImageService imageService;

    public ProductController() {
        this.productService = new ProductService();
        this.imageService = new ImageService();
    }

    public void getAllProducts(Context ctx) {
        try {
            String categoria = ctx.queryParam("categoria");
            String ciudad = ctx.queryParam("ciudad");
            String usuarioId = ctx.queryParam("usuario_id");

            List<ProductDTO> products;
            if (categoria != null || ciudad != null || usuarioId != null) {
                Integer userIdInt = usuarioId != null ? Integer.parseInt(usuarioId) : null;
                products = productService.getProductsWithFilters(categoria, ciudad, userIdInt);
            } else {
                products = productService.getAllProducts();
            }

            ctx.contentType("application/json");
            ctx.json(new ApiResponse(true, "Productos obtenidos exitosamente", Map.of("products", products)));
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getUserProducts(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<ProductDTO> products = productService.getUserProducts(userId);
            ctx.contentType("application/json");
            ctx.json(new ApiResponse(true, "Productos del usuario obtenidos", Map.of("products", products)));
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            ProductDTO product = productService.getProductById(productId);

            if (product != null) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Producto obtenido", Map.of("product", product)));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void debugProduct(Context ctx) {
        String sql = "SELECT id, nombre, usuario_id, descripcion, valor_estimado FROM productos WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int productId = Integer.parseInt(ctx.pathParam("id"));

            stmt.setInt(1, productId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> debug = new HashMap<>();
                debug.put("id", rs.getInt("id"));
                debug.put("nombre", rs.getString("nombre"));
                debug.put("usuario_id", rs.getInt("usuario_id"));
                debug.put("descripcion", rs.getString("descripcion"));
                debug.put("valor_estimado", rs.getDouble("valor_estimado"));

                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {

            e.printStackTrace();
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    public void createProduct(Context ctx) {
        try {

            }
            request.setNombreProducto(nombreProducto);

            String usuarioIdStr = ctx.formParam("usuario_id");
            if (usuarioIdStr == null) {
                ctx.status(400).json(new ApiResponse(false, "ID de usuario es obligatorio", null));
                return;
            }
            request.setUsuario_id(Integer.parseInt(usuarioIdStr));

            request.setDescripcionProducto(ctx.formParam("descripcionProducto"));

            String valorEstimadoStr = ctx.formParam("valorEstimado");
            if (valorEstimadoStr != null) {
                request.setValorEstimado(Double.parseDouble(valorEstimadoStr));
            }



            int productId = productService.createProduct(request, imageUrl);

            if (productId > 0) {

                ctx.status(500).json(new ApiResponse(false, "Error al crear producto en base de datos", null));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(new ApiResponse(false, "Error en el formato de los números. Asegúrate de que todos los campos numéricos sean válidos.", null));
        } catch (Exception e) {

            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error al crear producto: " + e.getMessage(), null));
        }
    }

    public void updateProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            ProductDTO currentProduct = productService.getProductById(productId);
            if (currentProduct == null) {
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
                return;
            }


            // Leer campos de form-data
            if (ctx.formParam("nombre") != null) {
                productToUpdate.setNombre(ctx.formParam("nombre"));
                hasFields = true;
            }
            if (ctx.formParam("descripcion") != null) {
                productToUpdate.setDescripcion(ctx.formParam("descripcion"));
                hasFields = true;
            }

            }
            if (ctx.formParam("categoria_id") != null) {
                productToUpdate.setCategoriaId(Integer.parseInt(ctx.formParam("categoria_id")));
                hasFields = true;
            }
            if (ctx.formParam("calidad_id") != null) {
                productToUpdate.setCalidadId(Integer.parseInt(ctx.formParam("calidad_id")));
                hasFields = true;
            }

            // Manejar la carga de la imagen
            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile != null) {
                String newImageUrl = imageService.uploadImage(uploadedFile, "productos");
                if (newImageUrl != null) {
                    productToUpdate.setImagen(newImageUrl);
                    // Opcional: eliminar imagen antigua de Cloudinary si existe
                    if (currentProduct.getImagen() != null) {
                        imageService.deleteImage(currentProduct.getImagen());
                    }
                }
                hasFields = true;
            }



            boolean success = productService.updateProduct(productId, productToUpdate);
            if (success) {

            } else {
                ctx.status(500).json(new ApiResponse(false, "Error al actualizar el producto", null));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(new ApiResponse(false, "Error en el formato de los números.", null));
        } catch (Exception e) {

            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error interno al actualizar el producto.", null));
        }
    }

    public void deleteProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));

            ProductDTO product = productService.getProductById(productId);

            boolean success = productService.deleteProduct(productId);

            if (success) {
                if (product != null && product.getImagen() != null && product.getImagen().contains("cloudinary.com")) {
                    boolean imageDeleted = imageService.deleteImage(product.getImagen());
                    if (imageDeleted) {
                        System.out.println("🗑️ Imagen eliminada de Cloudinary para producto ID: " + productId);
                    }
                }

                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Producto eliminado exitosamente", null));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al eliminar producto", null));
            e.printStackTrace();
        }
    }

    public void uploadProductImage(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));

            ProductDTO currentProduct = productService.getProductById(productId);
            if (currentProduct == null) {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
                return;
            }

            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile == null) {
                ctx.contentType("application/json");
                ctx.status(400).json(new ApiResponse(false, "No se encontró archivo de imagen", null));
                return;
            }

            System.out.println(" Subiendo imagen para producto ID: " + productId);
            System.out.println("   - Archivo: " + uploadedFile.filename() + " (" + uploadedFile.size() + " bytes)");

            String newImageUrl = imageService.uploadImage(uploadedFile, "productos");

            if (newImageUrl != null) {
                // Eliminar imagen anterior si existe
                if (currentProduct.getImagen() != null && currentProduct.getImagen().contains("cloudinary.com")) {
                    boolean deleted = imageService.deleteImage(currentProduct.getImagen());
                    if (deleted) {
                        System.out.println("🗑️ Imagen anterior eliminada");
                    }
                }

                // Actualizar producto con nueva imagen
                Product product = new Product();
                product.setImagen(newImageUrl);
                boolean success = productService.updateProduct(productId, product);

                if (success) {
                    System.out.println("✅ Imagen actualizada para producto ID: " + productId);
                    ctx.contentType("application/json");
                    ctx.json(new ApiResponse(true, "Imagen subida exitosamente",
                            Map.of("imageUrl", newImageUrl)));
                } else {
                    ctx.contentType("application/json");
                    ctx.status(500).json(new ApiResponse(false, "Error actualizando imagen en base de datos", null));
                }
            } else {
                ctx.contentType("application/json");
                ctx.status(400).json(new ApiResponse(false, "Error al subir imagen a Cloudinary", null));
            }
        } catch (Exception e) {
            System.err.println(" Error subiendo imagen: " + e.getMessage());
            e.printStackTrace();
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al subir imagen", null));
        }
    }
}