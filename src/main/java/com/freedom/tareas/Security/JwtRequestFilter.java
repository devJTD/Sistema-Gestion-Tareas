package com.freedom.tareas.Security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.freedom.tareas.Service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtRequestFilter extends OncePerRequestFilter {

    // Sección de Dependencias
    // Inyecta el servicio de usuario para cargar detalles del usuario.
    private final UserService userService;
    // Inyecta la utilidad JWT para operaciones de token.
    private final JwtUtil jwtUtil;

    // Constructor para inyección de dependencias (ahora manual desde SecurityConfig).
    public JwtRequestFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Sección de Filtrado de Solicitudes
    // Intercepta las solicitudes HTTP para validar el token JWT presente en el encabezado.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Extrae el JWT del encabezado "Authorization" si está presente y tiene el prefijo "Bearer ".
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            // --- DEBUG: Imprime el token JWT recibido con separación ---
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT: JWT recibido en el filtro para la ruta: " + request.getRequestURI());
            System.out.println("DEBUG JWT: Token: " + jwt);
            // --- FIN DEBUG ---
            try {
                username = jwtUtil.extractUsername(jwt);
                // --- DEBUG: Imprime el username extraído ---
                System.out.println("DEBUG JWT: Username extraído del JWT: " + username);
                // --- FIN DEBUG ---
            } catch (Exception e) {
                System.err.println("DEBUG JWT ERROR: Error al extraer username del JWT para la ruta " + request.getRequestURI() + ": " + e.getMessage());
            }
        } else {
            // Este mensaje ahora solo debería aparecer para rutas que *realmente* esperan un JWT
            // y no lo tienen.
            System.out.println("LOG: No se encontró encabezado 'Authorization' o no es un token Bearer para la ruta: " + request.getRequestURI());
        }

        // Si se extrajo un nombre de usuario y el contexto de seguridad no tiene una autenticación activa.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                userDetails = this.userService.loadUserByUsername(username);
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException |
                     org.springframework.security.authentication.DisabledException e) {
                System.err.println("DEBUG JWT ERROR: Usuario '" + username + "' no encontrado o deshabilitado al cargar UserDetails: " + e.getMessage());
            }

            // Valida el token JWT con los detalles del usuario cargados.
            if (userDetails != null && jwtUtil.validateToken(jwt, userDetails)) {
                // Crea un objeto de autenticación para Spring Security.
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // Establece detalles adicionales de la solicitud en el token de autenticación.
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Establece el objeto de autenticación en el SecurityContext de Spring.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                // --- DEBUG: Autenticación JWT exitosa ---
                System.out.println("DEBUG JWT: Autenticación JWT exitosa para usuario: " + username);
                System.out.println("************************************************************************************\n");
                // --- FIN DEBUG ---
            } else {
                System.out.println("DEBUG JWT: Validación de token fallida para usuario: " + username + " en la ruta: " + request.getRequestURI());
            }
        }

        // Permite que la solicitud continúe a través de la cadena de filtros.
        chain.doFilter(request, response);
    }
}
