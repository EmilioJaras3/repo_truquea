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
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));

            System.out.println("🔍DEBUG: Consultando producto ID " + productId + " directamente en BD...");

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, nombre, usuario_id, descripcion, valor_estimado FROM productos WHERE id = ?"
            );
            stmt.setInt(1, productId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> debug = new HashMap<>();
                debug.put("id", rs.getInt("id"));
                debug.put("nombre", rs.getString("nombre"));
                debug.put("usuario_id", rs.getInt("usuario_id"));
                debug.put("descripcion", rs.getString("descripcion"));
                debug.put("valor_estimado", rs.getDouble("valor_estimado"));

                System.out.println(" DEBUG: Producto encontrado - usuario_id: " + rs.getInt("usuario_id"));

                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Debug producto", debug));
            } else {
                System.out.println(" DEBUG: Producto no encontrado");
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }

            conn.close();
        } catch (Exception e) {
            System.err.println(" DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    public void createProduct(Context ctx) {
        try {
            System.out.println("🔍 === INICIANDO CREACIÓN DE PRODUCTO ===");

            // LEER JSON EN LUGAR DE FORM-DATA
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            System.out.println("📨 Body recibido: " + body);

            CreateProductRequestDTO request = new CreateProductRequestDTO();

            //  EXTRAER DATOS DEL JSON
            String nombreProducto = (String) body.get("nombreProducto");
            String descripcionProducto = (String) body.get("descripcionProducto");

            request.setNombreProducto(nombreProducto);
            request.setDescripcionProducto(descripcionProducto);

            //  MANEJAR NÚMEROS CON CONVERSIÓN SEGURA
            if (body.get("valorEstimado") != null) {
                Object valorObj = body.get("valorEstimado");
                if (valorObj instanceof Number) {
                    request.setValorEstimado(((Number) valorObj).doubleValue());
                } else if (valorObj instanceof String && !((String) valorObj).isEmpty()) {
                    request.setValorEstimado(Double.parseDouble((String) valorObj));
                }
            }

            if (body.get("idCategoria") != null) {
                Object categoriaObj = body.get("idCategoria");
                if (categoriaObj instanceof Number) {
                    request.setIdCategoria(((Number) categoriaObj).intValue());
                } else if (categoriaObj instanceof String && !((String) categoriaObj).isEmpty()) {
                    request.setIdCategoria(Integer.parseInt((String) categoriaObj));
                }
            }

            if (body.get("idCalidad") != null) {
                Object calidadObj = body.get("idCalidad");
                if (calidadObj instanceof Number) {
                    request.setIdCalidad(((Number) calidadObj).intValue());
                } else if (calidadObj instanceof String && !((String) calidadObj).isEmpty()) {
                    request.setIdCalidad(Integer.parseInt((String) calidadObj));
                }
            }

            if (body.get("usuario_id") != null) {
                Object userIdObj = body.get("usuario_id");
                if (userIdObj instanceof Number) {
                    request.setUsuario_id(((Number) userIdObj).intValue());
                } else if (userIdObj instanceof String && !((String) userIdObj).isEmpty()) {
                    request.setUsuario_id(Integer.parseInt((String) userIdObj));
                }
            }

            System.out.println(" Datos extraídos:");
            System.out.println("   - Nombre: " + request.getNombreProducto());
            System.out.println("   - Usuario ID: " + request.getUsuario_id());
            System.out.println("   - Categoría ID: " + request.getIdCategoria());
            System.out.println("   - Calidad ID: " + request.getIdCalidad());
            System.out.println("   - Valor: " + request.getValorEstimado());

            //  VALIDACIÓN
            if (request.getNombreProducto() == null || request.getNombreProducto().trim().isEmpty() || request.getUsuario_id() == 0) {
                System.out.println(" Faltan datos obligatorios");
                ctx.contentType("application/json");
                ctx.status(400).json(new ApiResponse(false, "Nombre del producto y usuario son obligatorios", null));
                return;
            }

            String imageUrl = null;
            System.out.println(" Producto sin imagen (JSON mode)");

            System.out.println(" Guardando en base de datos...");

            int productId = productService.createProduct(request, imageUrl);

            if (productId > 0) {
                System.out.println(" ¡PRODUCTO CREADO EXITOSAMENTE!");
                System.out.println("   - Product ID: " + productId);
                System.out.println("   - Nombre: " + request.getNombreProducto());
                System.out.println("   - Usuario: " + request.getUsuario_id());

                ctx.contentType("application/json");
                ctx.status(201).json(new ApiResponse(true, "Producto creado exitosamente",
                        Map.of("productId", productId, "imageUrl", imageUrl != null ? imageUrl : "")));
            } else {
                System.out.println(" Error: Base de datos no pudo crear el producto");
                ctx.contentType("application/json");
                ctx.status(500).json(new ApiResponse(false, "Error al crear producto en base de datos", null));
            }

            System.out.println("🔄 === FIN CREACIÓN PRODUCTO ===");

        } catch (Exception e) {
            System.out.println(" ERROR INESPERADO:");
            System.out.println("   - Tipo: " + e.getClass().getSimpleName());
            System.out.println("   - Mensaje: " + e.getMessage());
            e.printStackTrace();
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al crear producto: " + e.getMessage(), null));
        }
    }

    public void updateProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));

            ProductDTO currentProduct = productService.getProductById(productId);
            if (currentProduct == null) {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
                return;
            }

            //  LEER JSON EN LUGAR DE FORM-DATA
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            System.out.println("📨 Actualizando producto ID " + productId + " con data: " + body);

            Product product = new Product();

            //  EXTRAER DATOS DEL JSON
            String nombre = (String) body.get("nombre");
            String descripcion = (String) body.get("descripcion");

            if (nombre != null && !nombre.trim().isEmpty()) {
                product.setNombre(nombre);
            }
            if (descripcion != null && !descripcion.trim().isEmpty()) {
                product.setDescripcion(descripcion);
            }

            //  MANEJAR NÚMEROS
            if (body.get("valorEstimado") != null) {
                Object valorObj = body.get("valorEstimado");
                if (valorObj instanceof Number) {
                    product.setValorEstimado(((Number) valorObj).doubleValue());
                } else if (valorObj instanceof String && !((String) valorObj).isEmpty()) {
                    product.setValorEstimado(Double.parseDouble((String) valorObj));
                }
            }

            if (body.get("categoria_id") != null) {
                Object categoriaObj = body.get("categoria_id");
                if (categoriaObj instanceof Number) {
                    product.setCategoriaId(((Number) categoriaObj).intValue());
                } else if (categoriaObj instanceof String && !((String) categoriaObj).isEmpty()) {
                    product.setCategoriaId(Integer.parseInt((String) categoriaObj));
                }
            }

            if (body.get("calidad_id") != null) {
                Object calidadObj = body.get("calidad_id");
                if (calidadObj instanceof Number) {
                    product.setCalidadId(((Number) calidadObj).intValue());
                } else if (calidadObj instanceof String && !((String) calidadObj).isEmpty()) {
                    product.setCalidadId(Integer.parseInt((String) calidadObj));
                }
            }

            //  MANEJO DE IMAGEN - POR AHORA SIN IMAGEN (SOLO JSON)
            // En el futuro, si necesitas imágenes, puedes crear un endpoint separado para subir imágenes

            boolean success = productService.updateProduct(productId, product);
            if (success) {
                System.out.println(" Producto ID " + productId + " actualizado exitosamente");

                ctx.contentType("application/json");
                Map<String, Object> responseData = new HashMap<>();
                String finalImageUrl = currentProduct.getImagen() != null ? currentProduct.getImagen() : "";
                responseData.put("imageUrl", finalImageUrl);

                ctx.json(new ApiResponse(true, "Producto actualizado exitosamente", responseData));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            System.err.println(" Error actualizando producto: " + e.getMessage());
            e.printStackTrace();
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar producto", null));
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