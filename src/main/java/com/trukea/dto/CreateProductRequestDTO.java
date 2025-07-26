package com.trukea.dto;

public class CreateProductRequestDTO {
    private String nombreProducto;
    private String descripcionProducto;
    private double valorEstimado;
    private int idCategoria;
    private int idCalidad;
    private int usuario_id;

    // Constructores
    public CreateProductRequestDTO() {}

    // Getters y Setters
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getDescripcionProducto() { return descripcionProducto; }
    public void setDescripcionProducto(String descripcionProducto) { this.descripcionProducto = descripcionProducto; }
    public double getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(double valorEstimado) { this.valorEstimado = valorEstimado; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public int getIdCalidad() { return idCalidad; }
    public void setIdCalidad(int idCalidad) { this.idCalidad = idCalidad; }
    public int getUsuario_id() { return usuario_id; }
    public void setUsuario_id(int usuario_id) { this.usuario_id = usuario_id; }
}