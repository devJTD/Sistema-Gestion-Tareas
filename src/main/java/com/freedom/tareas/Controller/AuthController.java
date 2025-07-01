package com.freedom.tareas.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // === INICIO DE SESIÓN ===
    @GetMapping("/login")
    public String mostrarPaginaLogin(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            // Usuario ya autenticado, redirigir a inicio
            return "redirect:/";
        }
        return "login";
    }

    // === REGISTRO ===
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
