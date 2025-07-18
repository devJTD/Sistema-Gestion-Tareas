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

    // Inyecta los servicios de tareas y usuarios.
    private final TaskService taskService;
    private final UserService userService;

    // Constructor para la inyección de dependencias.
    public TrashController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // Muestra la vista de la papelera con las tareas "eliminadas".
    // Obtiene el usuario actual y carga sus tareas marcadas como "off".
    @GetMapping
    public String showTrash(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página de la papelera.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        List<Task> tasksInTrash = taskService.obtenerTareasEnPapelera(currentUser);

        // Calcula y loguea los días que cada tarea lleva en la papelera.
        tasksInTrash.forEach(task -> {
            if (task.getDeletedAt() != null) {
                long days = ChronoUnit.DAYS.between(task.getDeletedAt(), LocalDate.now());
                System.out.println("LOG: Tarea ID " + task.getId() + " lleva " + days + " días en la papelera.");
            }
        });

        model.addAttribute("tasks", tasksInTrash);
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUri", request.getRequestURI());
        System.out.println("LOG: Se cargaron " + tasksInTrash.size() + " tareas en la papelera para el usuario: " + username);
        return "trash";
    }

    // Maneja la solicitud para restaurar una tarea de la papelera.
    // Llama al servicio para cambiar el estado de la tarea a activa.
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

    // Maneja la solicitud para eliminar una tarea de forma permanente.
    // Invoca el servicio para remover la tarea definitivamente de la base de datos.
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