package com.freedom.tareas.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// CONFIGURACIÓN DE CONTRASEÑAS - Define el codificador de contraseñas usado en el sistema
@Configuration
public class PasswordConfig {

    // BEAN DE PasswordEncoder - Usa BCrypt para codificar y verificar contraseñas de forma segura
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("LOG: Configurando BCryptPasswordEncoder como PasswordEncoder.");
        return new BCryptPasswordEncoder();
    }
}
