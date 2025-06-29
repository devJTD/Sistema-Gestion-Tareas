package com.freedom.tareas.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    // Mostrar la página de perfil del usuario autenticado
    @GetMapping("/profile")
    public String mostrarPaginaPerfil() {
        return "profile";
    }
}
