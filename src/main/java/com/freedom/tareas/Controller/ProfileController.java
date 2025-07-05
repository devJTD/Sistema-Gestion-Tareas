package com.freedom.tareas.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String mostrarPerfil(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página de perfil.");
        model.addAttribute("currentUri", request.getRequestURI());

        // Obtener el usuario autenticado actual del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsernameFromAuth = authentication.getName();

        User currentUser = userService.buscarEntidadPorUsername(currentUsernameFromAuth)
                .orElse(null);

        if (currentUser == null) {
            System.out.println("LOG: Usuario autenticado '" + currentUsernameFromAuth + "' no encontrado en la base de datos, redirigiendo a login.");
            SecurityContextHolder.clearContext(); // Limpia el contexto de seguridad
            return "redirect:/login?logout"; // Redirige al login con parámetro de logout
        }

        System.out.println("LOG: Mostrando perfil para el usuario: " + currentUser.getUsername());
        model.addAttribute("user", currentUser);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String actualizarPerfil(
            User userDetailsFromForm,
            @RequestParam(value = "password", required = false) String newPassword,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request, // Añadir HttpServletRequest
            HttpServletResponse response) { // Añadir HttpServletResponse

        // Obtener el nombre de usuario actual ANTES de la actualización de la base de datos
        // Esto es crucial para detectar si el username ha cambiado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oldUsername = authentication.getName();

        System.out.println("LOG: Solicitud POST /profile/update. Nombre de usuario actual en sesión: " + oldUsername);
        System.out.println("LOG: Nuevo nombre de usuario enviado en el formulario: " + userDetailsFromForm.getUsername());

        try {
            // Llama al servicio para actualizar el perfil.
            // Asegúrate de que tu UserService.updateProfile devuelva el User actualizado.
            User updatedUser = userService.updateProfile(userDetailsFromForm, newPassword);
            System.out.println("LOG: Perfil del usuario " + updatedUser.getUsername() + " actualizado exitosamente en la base de datos.");

            // Comprobar si el nombre de usuario ha cambiado
            if (!oldUsername.equals(updatedUser.getUsername())) {
                System.out.println("LOG: El nombre de usuario ha cambiado de '" + oldUsername + "' a '" + updatedUser.getUsername() + "'. Se requerirá reiniciar la sesión.");
                // Realizar el logout explícitamente
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                redirectAttributes.addFlashAttribute("successMessage", "Su nombre de usuario ha cambiado. Por favor, inicie sesión con su nuevo nombre de usuario.");
                return "redirect:/login"; // Redirigir al login después del cambio de username
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
            }

        } catch (UserService.UserNotFoundException e) {
            System.err.println("LOG ERROR: Intento de actualizar perfil de usuario no encontrado: " + userDetailsFromForm.getUsername() + " - " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) { // Para capturar validaciones como username/email ya existen
            System.err.println("LOG ERROR: Error de validación al actualizar el perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            System.err.println("LOG ERROR: Error general al actualizar el perfil del usuario " + userDetailsFromForm.getUsername() + ": " + e.getMessage());
            // e.printStackTrace(); // Considerar logging the stack trace using a logger instead
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}