package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.User; 
import com.freedom.tareas.Service.UserService; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    private final UserService userService; 

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String mostrarPerfil(Model model, HttpServletRequest request) {
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);

        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {

            return "redirect:/login";
        }
        model.addAttribute("user", currentUser); 
        return "profile";
    }

    @PostMapping("/profile/update") 
    public String actualizarPerfil(
            User user, 
            @RequestParam(value = "password", required = false) String newPassword, 
               RedirectAttributes redirectAttributes) {

        try {

            userService.updateProfile(user, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
        } catch (UserService.UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }

        return "redirect:/profile"; 
    }
}