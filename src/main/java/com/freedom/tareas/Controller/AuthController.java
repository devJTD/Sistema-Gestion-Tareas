package com.freedom.tareas.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freedom.tareas.Security.JwtUtil;
import com.freedom.tareas.Service.UserService;
import com.freedom.tareas.dto.LoginRequest;
import com.freedom.tareas.dto.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class AuthController {

    // Sección de Dependencias
    // Inyecta los componentes necesarios para la autenticación y JWT.
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // Constructor de la clase
    // Inicializa las dependencias inyectadas.
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // Sección de Autenticación
    // Maneja las solicitudes de autenticación, valida credenciales, genera JWT y establece la sesión.
    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) throws Exception {
        System.out.println("LOG: Solicitud de autenticación para el usuario: " + authenticationRequest.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            System.out.println("LOG: Fallo de autenticación para usuario: " + authenticationRequest.getUsername() + " - Credenciales incorrectas.");
            return ResponseEntity.status(401).body(new LoginResponse(null, "Credenciales incorrectas"));
        }
        // Establece la autenticación en el SecurityContext y la sesión HTTP.
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        // Genera el token JWT.
        final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // --- DEBUG: Imprime el token JWT generado con separación ---
        System.out.println("\n************************************************************************************");
        System.out.println("DEBUG JWT: Token JWT generado para " + userDetails.getUsername() + ": " + jwt);
        System.out.println("************************************************************************************\n");
        // --- FIN DEBUG ---

        System.out.println("LOG: Autenticación exitosa para el usuario: " + userDetails.getUsername());
        // Devuelve el JWT en la respuesta.
        return ResponseEntity.ok(new LoginResponse(jwt, "Autenticación exitosa"));
    }
}
