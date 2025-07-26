// ===============================================
// PRODUCTSERVICE FALTANTE - src/main/java/com/trukea/service/ProductService.java
// ===============================================

package com.trukea.service;

import com.trukea.dto.ProductDTO;
import com.trukea.dto.CreateProductRequestDTO;
import com.trukea.models.Product;
import com.trukea.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    private ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsWithFilters(String categoria, String ciudad, Integer usuarioId) {
        return productRepository.findByFilters(categoria, ciudad, usuarioId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getUserProducts(int userId) {
        return productRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(int id) {
        Product product = productRepository.findById(id);
        if (product != null) {
            return convertToDTO(product);
        }
        return null;
    }

    public int createProduct(CreateProductRequestDTO request, String imageName) {
        Product product = new Product();
        product.setNombre(request.getNombreProducto());
        product.setDescripcion(request.getDescripcionProducto());
        product.setValorEstimado(request.getValorEstimado());
        product.setCategoriaId(request.getIdCategoria());
        product.setCalidadId(request.getIdCalidad());
        product.setUsuarioId(request.getUsuario_id());
        product.setImagen(imageName);

        return productRepository.save(product);
    }

    public boolean updateProduct(int id, Product product) {
        return productRepository.update(id, product);
    }

    public boolean deleteProduct(int id) {
        return productRepository.deleteById(id);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setNombre(product.getNombre());
        dto.setDescripcion(product.getDescripcion());
        dto.setValorEstimado(product.getValorEstimado());
        dto.setImagen(product.getImagen());
        dto.setCategoriaNombre(product.getCategoriaNombre());
        dto.setCalidadNombre(product.getCalidadNombre());
        dto.setUsuarioNombre(product.getUsuarioNombre());
        dto.setUsuarioApellido(product.getUsuarioApellido());
        dto.setCiudadNombre(product.getCiudadNombre());
        dto.setDisponible(product.isDisponible());
        return dto;
    }
}