package com.trukea.models;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int truequeId;
    private int calificadorId;
    private int calificadoId;
    private int puntuacion;
    private String comentario;
    private LocalDateTime createdAt;

    // Campos adicionales para joins
    private String calificadorNombre;
    private String calificadorApellido;

    // Constructores
    public Rating() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTruequeId() { return truequeId; }
    public void setTruequeId(int truequeId) { this.truequeId = truequeId; }
    public int getCalificadorId() { return calificadorId; }
    public void setCalificadorId(int calificadorId) { this.calificadorId = calificadorId; }
    public int getCalificadoId() { return calificadoId; }
    public void setCalificadoId(int calificadoId) { this.calificadoId = calificadoId; }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCalificadorNombre() { return calificadorNombre; }
    public void setCalificadorNombre(String calificadorNombre) { this.calificadorNombre = calificadorNombre; }
    public String getCalificadorApellido() { return calificadorApellido; }
    public void setCalificadorApellido(String calificadorApellido) { this.calificadorApellido = calificadorApellido; }
}