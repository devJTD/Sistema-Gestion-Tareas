package com.freedom.tareas.Security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Se invoca cuando un usuario no autenticado intenta acceder a un recurso protegido.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        System.err.println("\n************************************************************************************");
        System.err.println("DEBUG JWT ERROR: Error de autenticación JWT: " + authException.getMessage());
        System.err.println("DEBUG JWT ERROR: Ruta solicitada: " + request.getRequestURI());
        System.err.println("************************************************************************************\n");

        // Establece el estado HTTP a 401 (No autorizado).
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Establece el tipo de contenido de la respuesta a JSON.
        response.setContentType("application/json");
        // Escribe un mensaje de error JSON en el cuerpo de la respuesta.
        response.getWriter().write("{ \"error\": \"No autorizado\", \"message\": \"" + authException.getMessage() + "\" }");
    }
}
