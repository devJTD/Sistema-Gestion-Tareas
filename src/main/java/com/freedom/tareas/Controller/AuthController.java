package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.Usuario;
import com.freedom.tareas.Service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        return "login";
    }

    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@ModelAttribute("usuario") Usuario usuario, Model model) {
        boolean creado = userService.crearUsuario(usuario);

        if (!creado) {
            model.addAttribute("error", "El nombre de usuario o correo ya están registrados.");
            return "register";
        }

        return "redirect:/login";
    }
}
