package com.trukea.models;

import java.time.LocalDateTime;

public class Trade {
    private int id;
    private int productoOfrecidoId;
    private int productoDeseadoId;
    private int usuarioOferenteId;
    private int usuarioReceptorId;
    private String comentario;
    private int estadoId;
    private LocalDateTime fechaPropuesta;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaCompletado;

    // Campos adicionales para joins
    private String productoOfrecidoNombre;
    private String productoOfrecidoImagen;
    private String productoDeseadoNombre;
    private String productoDeseadoImagen;
    private String oferenteNombre;
    private String oferenteApellido;
    private String receptorNombre;
    private String receptorApellido;
    private String estadoNombre;

    // Constructores
    public Trade() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductoOfrecidoId() { return productoOfrecidoId; }
    public void setProductoOfrecidoId(int productoOfrecidoId) { this.productoOfrecidoId = productoOfrecidoId; }
    public int getProductoDeseadoId() { return productoDeseadoId; }
    public void setProductoDeseadoId(int productoDeseadoId) { this.productoDeseadoId = productoDeseadoId; }
    public int getUsuarioOferenteId() { return usuarioOferenteId; }
    public void setUsuarioOferenteId(int usuarioOferenteId) { this.usuarioOferenteId = usuarioOferenteId; }
    public int getUsuarioReceptorId() { return usuarioReceptorId; }
    public void setUsuarioReceptorId(int usuarioReceptorId) { this.usuarioReceptorId = usuarioReceptorId; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public int getEstadoId() { return estadoId; }
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }

    // Getters y setters para campos adicionales
    public String getProductoOfrecidoNombre() { return productoOfrecidoNombre; }
    public void setProductoOfrecidoNombre(String productoOfrecidoNombre) { this.productoOfrecidoNombre = productoOfrecidoNombre; }
    public String getProductoOfrecidoImagen() { return productoOfrecidoImagen; }
    public void setProductoOfrecidoImagen(String productoOfrecidoImagen) { this.productoOfrecidoImagen = productoOfrecidoImagen; }
    public String getProductoDeseadoNombre() { return productoDeseadoNombre; }
    public void setProductoDeseadoNombre(String productoDeseadoNombre) { this.productoDeseadoNombre = productoDeseadoNombre; }
    public String getProductoDeseadoImagen() { return productoDeseadoImagen; }
    public void setProductoDeseadoImagen(String productoDeseadoImagen) { this.productoDeseadoImagen = productoDeseadoImagen; }
    public String getOferenteNombre() { return oferenteNombre; }
    public void setOferenteNombre(String oferenteNombre) { this.oferenteNombre = oferenteNombre; }
    public String getOferenteApellido() { return oferenteApellido; }
    public void setOferenteApellido(String oferenteApellido) { this.oferenteApellido = oferenteApellido; }
    public String getReceptorNombre() { return receptorNombre; }
    public void setReceptorNombre(String receptorNombre) { this.receptorNombre = receptorNombre; }
    public String getReceptorApellido() { return receptorApellido; }
    public void setReceptorApellido(String receptorApellido) { this.receptorApellido = receptorApellido; }
    public String getEstadoNombre() { return estadoNombre; }
    public void setEstadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; }
}