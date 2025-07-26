package com.trukea.models;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String nombre;
    private String descripcion;
    private double valorEstimado;
    private String imagen;
    private int categoriaId;
    private int calidadId;
    private int usuarioId;
    private boolean disponible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos adicionales para joins
    private String categoriaNombre;
    private String calidadNombre;
    private String usuarioNombre;
    private String usuarioApellido;
    private String ciudadNombre;

    // Constructores
    public Product() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(double valorEstimado) { this.valorEstimado = valorEstimado; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }
    public int getCalidadId() { return calidadId; }
    public void setCalidadId(int calidadId) { this.calidadId = calidadId; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
    public String getCalidadNombre() { return calidadNombre; }
    public void setCalidadNombre(String calidadNombre) { this.calidadNombre = calidadNombre; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public String getUsuarioApellido() { return usuarioApellido; }
    public void setUsuarioApellido(String usuarioApellido) { this.usuarioApellido = usuarioApellido; }
    public String getCiudadNombre() { return ciudadNombre; }
    public void setCiudadNombre(String ciudadNombre) { this.ciudadNombre = ciudadNombre; }
}
