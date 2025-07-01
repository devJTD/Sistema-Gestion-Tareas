package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
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