package com.freedom.tareas.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Obtener las autoridades (roles) del usuario autenticado
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Verificar si el usuario tiene el rol ADMIN
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Lógica de redirección
        if (isAdmin) {
            // Si es ADMIN, redirige siempre a /admin
            response.sendRedirect("/admin");
        } else {
            // Si no es ADMIN (es decir, es USER), redirige siempre a /
            response.sendRedirect("/");
        }

        // Eliminamos el bloque `if (request.getRequestURI().equals("/login"))`
        // porque la lógica de redirección ya es universal para ambos casos
        // (tanto para el login inicial como para un re-login a la página /login).
        // El AuthenticationSuccessHandler solo se activa si el login fue exitoso.
    }
}