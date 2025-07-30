package com.trukea.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import com.trukea.ApiResponse;
import com.trukea.service.ProductService;
import com.trukea.service.ImageService;
import com.trukea.dto.ProductDTO;
import com.trukea.dto.CreateProductRequestDTO;
import com.trukea.models.Product;
import java.util.List;
import java.util.Map;

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

    public void createProduct(Context ctx) {
        try {
            // ✅ LOG: Inicio del proceso
            System.out.println("🔍 Iniciando creación de producto...");

            CreateProductRequestDTO request = new CreateProductRequestDTO();
            request.setNombreProducto(ctx.formParam("nombreProducto"));
            request.setDescripcionProducto(ctx.formParam("descripcionProducto"));

            String valorStr = ctx.formParam("valorEstimado");
            if (valorStr != null && !valorStr.isEmpty()) {
                request.setValorEstimado(Double.parseDouble(valorStr));
            }

            String categoriaStr = ctx.formParam("idCategoria");
            if (categoriaStr != null && !categoriaStr.isEmpty()) {
                request.setIdCategoria(Integer.parseInt(categoriaStr));
            }

            String calidadStr = ctx.formParam("idCalidad");
            if (calidadStr != null && !calidadStr.isEmpty()) {
                request.setIdCalidad(Integer.parseInt(calidadStr));
            }

            String userIdStr = ctx.formParam("usuario_id");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                request.setUsuario_id(Integer.parseInt(userIdStr));
            }

            // ✅ LOG: Datos recibidos
            System.out.println("📝 Producto: " + request.getNombreProducto() + " | Usuario: " + request.getUsuario_id());

            if (request.getNombreProducto() == null || request.getUsuario_id() == 0) {
                System.out.println("❌ Faltan datos obligatorios");
                ctx.contentType("application/json");
                ctx.status(400).json(new ApiResponse(false, "Nombre del producto y usuario son obligatorios", null));
                return;
            }

            String imageUrl = null;
            UploadedFile uploadedFile = ctx.uploadedFile("imagen");

            if (uploadedFile != null) {
                // ✅ LOG: Imagen recibida
                System.out.println("📸 Imagen recibida: " + uploadedFile.filename() + " (" + uploadedFile.size() + " bytes)");

                imageUrl = imageService.uploadImage(uploadedFile, "productos");

                if (imageUrl == null) {
                    System.out.println("❌ Fallo al subir imagen a Cloudinary");
                    ctx.contentType("application/json");
                    ctx.status(400).json(new ApiResponse(false, "Error al subir la imagen. Verifica el formato y tamaño.", null));
                    return;
                } else {
                    // ✅ LOG: Imagen subida exitosamente
                    System.out.println("✅ Imagen en Cloudinary: " + imageUrl);
                }
            } else {
                System.out.println("📝 Producto sin imagen");
            }

            // ✅ LOG: Guardando en BD
            System.out.println("💾 Guardando en base de datos...");

            int productId = productService.createProduct(request, imageUrl);

            if (productId > 0) {
                // ✅ LOG: Éxito total
                System.out.println("🎉 Producto creado exitosamente - ID: " + productId);
                ctx.contentType("application/json");
                ctx.status(201).json(new ApiResponse(true, "Producto creado exitosamente",
                        Map.of("productId", productId, "imageUrl", imageUrl != null ? imageUrl : "")));
            } else {
                // ✅ LOG: Error en BD
                System.out.println("❌ Error: Base de datos no pudo crear el producto");
                ctx.contentType("application/json");
                ctx.status(500).json(new ApiResponse(false, "Error al crear producto en base de datos", null));
            }

        } catch (Exception e) {
            // ✅ LOG: Error general
            System.out.println("💥 Error inesperado: " + e.getMessage());
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

            Product product = new Product();
            product.setNombre(ctx.formParam("nombre"));
            product.setDescripcion(ctx.formParam("descripcion"));

            String valorStr = ctx.formParam("valorEstimado");
            if (valorStr != null && !valorStr.isEmpty()) {
                product.setValorEstimado(Double.parseDouble(valorStr));
            }

            String categoriaStr = ctx.formParam("categoria_id");
            if (categoriaStr != null && !categoriaStr.isEmpty()) {
                product.setCategoriaId(Integer.parseInt(categoriaStr));
            }

            String calidadStr = ctx.formParam("calidad_id");
            if (calidadStr != null && !calidadStr.isEmpty()) {
                product.setCalidadId(Integer.parseInt(calidadStr));
            }

            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile != null) {
                System.out.println("🔄 Actualizando imagen para producto ID: " + productId);

                String newImageUrl = imageService.uploadImage(uploadedFile, "productos");

                if (newImageUrl != null) {
                    if (currentProduct.getImagen() != null && currentProduct.getImagen().contains("cloudinary.com")) {
                        boolean deleted = imageService.deleteImage(currentProduct.getImagen());
                        if (deleted) {
                            System.out.println("🗑️ Imagen anterior eliminada");
                        }
                    }

                    product.setImagen(newImageUrl);
                    System.out.println("✅ Nueva imagen actualizada");
                } else {
                    ctx.contentType("application/json");
                    ctx.status(400).json(new ApiResponse(false, "Error al subir la nueva imagen", null));
                    return;
                }
            }

            boolean success = productService.updateProduct(productId, product);
            if (success) {
                ctx.contentType("application/json");
                ctx.json(new ApiResponse(true, "Producto actualizado exitosamente",
                        Map.of("imageUrl", product.getImagen() != null ? product.getImagen() : currentProduct.getImagen())));
            } else {
                ctx.contentType("application/json");
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.contentType("application/json");
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar producto", null));
            e.printStackTrace();
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
}