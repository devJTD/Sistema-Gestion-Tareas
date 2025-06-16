package com.freedom.tareas.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    @GetMapping("/profile")
    public String showProfilePage() {
        return "profile"; // Ajusta este path si tu archivo HTML está en otra ubicación
    }
}
