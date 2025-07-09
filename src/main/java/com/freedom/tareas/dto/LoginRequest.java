package com.freedom.tareas.dto;

public class LoginRequest {

    // Define los campos para el nombre de usuario y la contraseña de la solicitud de login.
    private String username;
    private String password;

    // Constructor vacío necesario para la deserialización JSON.
    public LoginRequest() {
    }

    // Constructor con todos los campos para inicializar el objeto.
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Obtiene el nombre de usuario.
    public String getUsername() {
        return username;
    }

    // Obtiene la contraseña.
    public String getPassword() {
        return password;
    }

    // Establece el nombre de usuario.
    public void setUsername(String username) {
        this.username = username;
    }

    // Establece la contraseña.
    public void setPassword(String password) {
        this.password = password;
    }

    // Proporciona una representación en cadena del objeto, ocultando la contraseña.
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
