package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        // Obtiene la información de autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario ya está autenticado
        // `isAuthenticated()` verifica que no sea anónimo ni nulo
        if (authentication != null && authentication.isAuthenticated() &&
            // Importante: También verifica que no sea una autenticación anónima,
            // que Spring Security usa para usuarios no autenticados por defecto
            !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            // Si el usuario ya está logueado, redirigirlo a la página principal
            return "redirect:/";
        }
        // Si no está logueado, muestra la página de login
        return "login"; // Asume que tienes una vista llamada 'login.html' o 'login.jsp'
    }

    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new User());
        return "register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@ModelAttribute("usuario") User usuario, Model model) {
        boolean creado = userService.crearUsuario(usuario);

        if (!creado) {
            model.addAttribute("error", "El nombre de usuario o correo ya están registrados.");
            return "register";
        }

        return "redirect:/login";
    }
}
