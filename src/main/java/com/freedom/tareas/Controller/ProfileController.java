package com.freedom.tareas.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    // Sección de Dependencias
    // Inyecta el servicio de usuario para operaciones relacionadas con el perfil.
    private final UserService userService;

    // Constructor de la clase
    // Inicializa el servicio de usuario.
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Sección de Visualización del Perfil
    // Muestra la página de perfil del usuario autenticado.
    @GetMapping("/profile")
    public String mostrarPerfil(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página de perfil.");
        model.addAttribute("currentUri", request.getRequestURI());
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            System.out.println("LOG: Usuario no autenticado, redirigiendo a login desde perfil.");
            return "redirect:/login";
        }
        System.out.println("LOG: Mostrando perfil para el usuario: " + currentUser.getUsername());
        model.addAttribute("user", currentUser);
        return "profile";
    }

    // Sección de Actualización del Perfil
    // Procesa la solicitud para actualizar la información del perfil del usuario.
    @PostMapping("/profile/update")
    public String actualizarPerfil(
            User user,
            @RequestParam(value = "password", required = false) String newPassword,
            RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para actualizar perfil del usuario: " + user.getUsername());
        try {
            userService.updateProfile(user, newPassword);
            System.out.println("LOG: Perfil del usuario " + user.getUsername() + " actualizado exitosamente.");
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
        } catch (UserService.UserNotFoundException e) {
            System.err.println("LOG ERROR: Intento de actualizar perfil de usuario no encontrado: " + user.getUsername() + " - " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            System.err.println("LOG ERROR: Error al actualizar el perfil del usuario " + user.getUsername() + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
