package com.trukea.dto;

import java.time.LocalDateTime;

public class TradeDTO {
    private int id;
    private String productoOfrecidoNombre;
    private String productoOfrecidoImagen;
    private String productoDeseadoNombre;
    private String productoDeseadoImagen;
    private String usuarioNombre;
    private String usuarioApellido;
    private String estadoNombre;
    private String comentario;
    private LocalDateTime fechaPropuesta;

    // Constructores
    public TradeDTO() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductoOfrecidoNombre() { return productoOfrecidoNombre; }
    public void setProductoOfrecidoNombre(String productoOfrecidoNombre) { this.productoOfrecidoNombre = productoOfrecidoNombre; }
    public String getProductoOfrecidoImagen() { return productoOfrecidoImagen; }
    public void setProductoOfrecidoImagen(String productoOfrecidoImagen) { this.productoOfrecidoImagen = productoOfrecidoImagen; }
    public String getProductoDeseadoNombre() { return productoDeseadoNombre; }
    public void setProductoDeseadoNombre(String productoDeseadoNombre) { this.productoDeseadoNombre = productoDeseadoNombre; }
    public String getProductoDeseadoImagen() { return productoDeseadoImagen; }
    public void setProductoDeseadoImagen(String productoDeseadoImagen) { this.productoDeseadoImagen = productoDeseadoImagen; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public String getUsuarioApellido() { return usuarioApellido; }
    public void setUsuarioApellido(String usuarioApellido) { this.usuarioApellido = usuarioApellido; }
    public String getEstadoNombre() { return estadoNombre; }
    public void setEstadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }
}