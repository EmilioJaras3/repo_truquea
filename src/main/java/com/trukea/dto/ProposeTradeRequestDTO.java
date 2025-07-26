package com.trukea.dto;

public class ProposeTradeRequestDTO {
    private int producto_ofrecido_id;
    private int producto_deseado_id;
    private int usuario_oferente_id;
    private String comentario;

    // Constructores
    public ProposeTradeRequestDTO() {}

    // Getters y Setters
    public int getProducto_ofrecido_id() { return producto_ofrecido_id; }
    public void setProducto_ofrecido_id(int producto_ofrecido_id) { this.producto_ofrecido_id = producto_ofrecido_id; }
    public int getProducto_deseado_id() { return producto_deseado_id; }
    public void setProducto_deseado_id(int producto_deseado_id) { this.producto_deseado_id = producto_deseado_id; }
    public int getUsuario_oferente_id() { return usuario_oferente_id; }
    public void setUsuario_oferente_id(int usuario_oferente_id) { this.usuario_oferente_id = usuario_oferente_id; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
