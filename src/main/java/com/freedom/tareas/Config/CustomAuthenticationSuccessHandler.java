package com.freedom.tareas.Config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// MANEJADOR PERSONALIZADO DE AUTENTICACIÓN EXITOSA - Redirige según el rol del usuario tras login exitoso
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // MÉTODO onAuthenticationSuccess - Se ejecuta tras autenticación correcta
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // REDIRECCIÓN DESDE LOGIN - Verifica si el usuario es ADMIN para decidir redirección
        if (request.getRequestURI().equals("/login")) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/");
            }
            return;
        }

        // REDIRECCIÓN GENERAL - Repite verificación por si login fue accedido desde otra ruta
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            response.sendRedirect("/admin");
        } else {
            response.sendRedirect("/");
        }
    }
}
