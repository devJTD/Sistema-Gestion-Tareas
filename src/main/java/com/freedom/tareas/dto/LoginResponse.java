package com.freedom.tareas.dto; // Asegúrate de que este sea el paquete correcto para tus DTOs

// Importaciones necesarias para las anotaciones de Lombok si las usas,
// o para los getters/setters si los generas manualmente.
// import lombok.Getter;
// import lombok.Setter;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// Esta clase representa el cuerpo de la respuesta (response body)
// que el servidor enviará al cliente después de una autenticación exitosa.
// Contendrá el token JWT.
public class LoginResponse {

    private String jwt;
    private String message; // Opcional: para mensajes de éxito

    // Constructor vacío (necesario para la serialización JSON)
    public LoginResponse() {
    }

    // Constructor con el token JWT
    public LoginResponse(String jwt) {
        this.jwt = jwt;
        this.message = "Autenticación exitosa"; // Mensaje por defecto
    }

    // Constructor con token y mensaje
    public LoginResponse(String jwt, String message) {
        this.jwt = jwt;
        this.message = message;
    }

    // Getter para el token JWT
    public String getJwt() {
        return jwt;
    }

    // Setter para el token JWT
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    // Getter para el mensaje
    public String getMessage() {
        return message;
    }

    // Setter para el mensaje
    public void setMessage(String message) {
        this.message = message;
    }

    // Opcional: Método toString para facilitar la depuración
    @Override
    public String toString() {
        return "LoginResponse{" +
               "jwt='" + jwt.substring(0, Math.min(jwt.length(), 30)) + "...' " + // Mostrar solo el inicio del token
               ", message='" + message + '\'' +
               '}';
    }
}
