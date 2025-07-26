package com.trukea.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import com.trukea.ApiResponse;
import com.trukea.service.ProductService;
import com.trukea.dto.ProductDTO;
import com.trukea.dto.CreateProductRequestDTO;
import com.trukea.models.Product;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProductController {
    private ProductService productService;

    public ProductController() {
        this.productService = new ProductService();
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

            ctx.json(new ApiResponse(true, "Productos obtenidos exitosamente", Map.of("products", products)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getUserProducts(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<ProductDTO> products = productService.getUserProducts(userId);
            ctx.json(new ApiResponse(true, "Productos del usuario obtenidos", Map.of("products", products)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            ProductDTO product = productService.getProductById(productId);

            if (product != null) {
                ctx.json(new ApiResponse(true, "Producto obtenido", Map.of("product", product)));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void createProduct(Context ctx) {
        try {
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

            if (request.getNombreProducto() == null || request.getUsuario_id() == 0) {
                ctx.status(400).json(new ApiResponse(false, "Nombre del producto y usuario son obligatorios", null));
                return;
            }

            String imageName = null;
            UploadedFile uploadedFile = ctx.uploadedFile("imagen");
            if (uploadedFile != null) {
                imageName = saveUploadedFile(uploadedFile);
            }

            int productId = productService.createProduct(request, imageName);
            if (productId > 0) {
                ctx.status(201).json(new ApiResponse(true, "Producto creado exitosamente",
                        Map.of("productId", productId)));
            } else {
                ctx.status(500).json(new ApiResponse(false, "Error al crear producto", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al crear producto", null));
            e.printStackTrace();
        }
    }

    public void updateProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));

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
                String imageName = saveUploadedFile(uploadedFile);
                if (imageName != null) {
                    product.setImagen(imageName);
                }
            }

            boolean success = productService.updateProduct(productId, product);
            if (success) {
                ctx.json(new ApiResponse(true, "Producto actualizado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar producto", null));
            e.printStackTrace();
        }
    }

    public void deleteProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            boolean success = productService.deleteProduct(productId);

            if (success) {
                ctx.json(new ApiResponse(true, "Producto eliminado exitosamente", null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Producto no encontrado", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al eliminar producto", null));
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