package com.freedom.tareas.Security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // Almacena la clave secreta JWT.
    private String secret;

    // Almacena el tiempo de expiración del JWT en milisegundos.
    private long expiration;

    // Obtiene la clave secreta JWT.
    public String getSecret() {
        return secret;
    }

    // Obtiene el tiempo de expiración del JWT.
    public long getExpiration() {
        return expiration;
    }

    // Establece la clave secreta JWT.
    public void setSecret(String secret) {
        this.secret = secret;
    }

    // Establece el tiempo de expiración del JWT.
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
