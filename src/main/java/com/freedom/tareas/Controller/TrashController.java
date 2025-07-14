package com.freedom.tareas.Controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.TaskService;
import com.freedom.tareas.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/trash")
public class TrashController {

    private final TaskService taskService;
    private final UserService userService;

    public TrashController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // Muestra la página de la papelera con las tareas eliminadas
    @GetMapping
    public String showTrash(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página de la papelera.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        List<Task> tasksInTrash = taskService.obtenerTareasEnPapelera(currentUser);

        // Calcular los días transcurridos desde la eliminación para cada tarea
        tasksInTrash.forEach(task -> {
            if (task.getDeletedAt() != null) {
                long days = ChronoUnit.DAYS.between(task.getDeletedAt(), LocalDate.now());
                // Puedes añadir esto como un atributo transitorio en la Task si usas Thymeleaf para mostrarlo directamente
                // O procesarlo en el frontend
                System.out.println("LOG: Tarea ID " + task.getId() + " lleva " + days + " días en la papelera.");
            }
        });

        model.addAttribute("tasks", tasksInTrash);
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUri", request.getRequestURI());
        System.out.println("LOG: Se cargaron " + tasksInTrash.size() + " tareas en la papelera para el usuario: " + username);
        return "trash";
    }

    // Restaura una tarea de la papelera
    @PostMapping("/restore/{id}")
    public String restoreTask(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para restaurar tarea con ID: " + id + " de la papelera.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        try {
            taskService.restaurarTarea(id, currentUser);
            System.out.println("LOG: Tarea con ID " + id + " restaurada exitosamente por el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea restaurada exitosamente!");
        } catch (IllegalArgumentException e) {
            System.err.println("LOG ERROR: Error al restaurar tarea con ID " + id + " para el usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trash";
    }

    // Elimina permanentemente una tarea de la papelera
    @PostMapping("/delete-permanent/{id}")
    public String deletePermanent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para eliminar permanentemente tarea con ID: " + id + " de la papelera.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        try {
            taskService.eliminarTareaPermanentemente(id, currentUser);
            System.out.println("LOG: Tarea con ID " + id + " eliminada permanentemente por el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea eliminada permanentemente!");
        } catch (IllegalArgumentException e) {
            System.err.println("LOG ERROR: Error al eliminar permanentemente tarea con ID " + id + " para el usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trash";
    }
}