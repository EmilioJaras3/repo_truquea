package com.trukea.dto;

public class LoginRequestDTO {
    private String correo;
    private String contrasena;

    // Constructores
    public LoginRequestDTO() {}

    // Getters y Setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
