package com.freedom.tareas.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User; 
import com.freedom.tareas.Service.TaskService;
import com.freedom.tareas.Service.UserService; 
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService; // Inyecta el UserService
    private final List<String> organizationTips = new ArrayList<>();

    public TaskController(TaskService taskService, UserService userService) { 
        this.taskService = taskService;
        this.userService = userService; 
        initializeTips();
    }

    private void initializeTips() {
        organizationTips.add("Define tus 3 tareas más importantes del día y concéntrate en ellas primero.");
        organizationTips.add("Divide las tareas grandes en pasos más pequeños y manejables.");
        organizationTips.add("Usa la técnica Pomodoro: trabaja 25 minutos, descansa 5.");
        organizationTips.add("Mantén tu espacio de trabajo ordenado para minimizar distracciones.");
        organizationTips.add("Revisa tu lista de tareas al final del día para planificar el siguiente.");
        organizationTips.add("Agrupa tareas similares para hacerlas juntas y ahorrar tiempo.");
        organizationTips.add("No pospongas las tareas importantes, abórdalas cuanto antes.");
        organizationTips.add("Delega si es posible. No tienes que hacerlo todo tú.");
        organizationTips.add("Establece plazos realistas para tus tareas.");
        organizationTips.add("Celebra tus logros al completar tareas.");
    }

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userService.findByUsernameEntity(username) 
                .orElse(null); 

        List<Task> allTasks = new ArrayList<>();
        if (currentUser != null) {
            allTasks = taskService.getTasksByUser(currentUser);
        }

        model.addAttribute("totalTasksCount", allTasks.size());
        model.addAttribute("completedTasksCount",
                allTasks.stream().filter(t -> "Completada".equalsIgnoreCase(t.getStatus())).count());
        model.addAttribute("pendingTasksCount",
                allTasks.stream().filter(t -> !"Completada".equalsIgnoreCase(t.getStatus())).count());
        model.addAttribute("currentUri", request.getRequestURI());

        LocalDate today = LocalDate.now();
        LocalDate twoDaysFromNow = today.plusDays(2);

        List<Task> tasksDueSoon = taskService.getDueSoonTasksByUser(currentUser, today, twoDaysFromNow); 

        model.addAttribute("tasksDueSoon", tasksDueSoon);

        Random random = new Random();
        if (!organizationTips.isEmpty()) {
            int randomIndex = random.nextInt(organizationTips.size());
            String randomTip = organizationTips.get(randomIndex);
            model.addAttribute("currentTip", randomTip);
            model.addAttribute("allTips", organizationTips);
        } else {
            model.addAttribute("currentTip", "No hay consejos disponibles.");
            model.addAttribute("allTips", new ArrayList<>());
        }

        return "index";
    }

    @GetMapping("/tasks")
    public String listTasks(
            Model model,
            HttpServletRequest request,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            @RequestParam(value = "priorityFilter", required = false) String priorityFilter,
            @RequestParam(value = "etiquetaFilter", required = false) String etiquetaFilter,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsernameEntity(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<Task> filteredTasks;

        filteredTasks = taskService.getFilteredAndSortedTasksByUser(currentUser, search, statusFilter, priorityFilter,
                etiquetaFilter, sortBy, sortDir);

        model.addAttribute("tasks", filteredTasks);
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("priorityFilter", priorityFilter);
        model.addAttribute("etiquetaFilter", etiquetaFilter);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "tasks";
    }

    @GetMapping("/calendar")
    public String calendarPage(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "calendar";
    }

    @GetMapping("/api/tasks-calendar")
    @ResponseBody
    public List<Map<String, Object>> getTasksForCalendar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsernameEntity(username) 
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<Task> allTasks = taskService.getTasksByUser(currentUser);

        return allTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .map(task -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", task.getId());
                    event.put("title", task.getTitle());
                    event.put("start", task.getDueDate());

                    String color = "#007bff";
                    if ("Completada".equalsIgnoreCase(task.getStatus())) {
                        color = "#28a745";
                    } else if ("Alta".equalsIgnoreCase(task.getPriority())) {
                        color = "#dc3545";
                    } else if ("Media".equalsIgnoreCase(task.getPriority())) {
                        color = "#ffc107";
                    }
                    event.put("color", color);

                    Map<String, Object> extendedProps = new HashMap<>();
                    extendedProps.put("description", task.getDescription());
                    extendedProps.put("priority", task.getPriority());
                    extendedProps.put("status", task.getStatus());
                    extendedProps.put("etiqueta", task.getEtiqueta());
                    event.put("extendedProps", extendedProps);

                    return event;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/tasks/add")
    public String showAddTaskForm(Model model, HttpServletRequest request) {
        model.addAttribute("taskFormObject", new Task());
        model.addAttribute("currentUri", request.getRequestURI());
        return "add-task";
    }

    @PostMapping("/tasks/save")
    public String saveTask(@Valid @ModelAttribute("taskFormObject") Task task,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> System.out.println("ERROR -> " + error));
            model.addAttribute("currentUri", "/tasks/add");
            return "add-task";
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); 
                                                       

            User currentUser = userService.findByUsernameEntity(username)
                    .orElseThrow(() -> new RuntimeException(
                            "Usuario autenticado no encontrado en la base de datos: " + username));

            task.setUsuario(currentUser); 

            if (task.getActiveOnPage() == null || task.getActiveOnPage().isEmpty()) {
                task.setActiveOnPage("on");            }

            taskService.createTask(task); 
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea agregada exitosamente!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar tarea: " + e.getMessage());
            model.addAttribute("currentUri", "/tasks/add");
            model.addAttribute("taskFormObject", task);
            return "add-task"; 
        }
        return "redirect:/tasks"; 
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsernameEntity(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        try {
            taskService.deleteTaskForUser(id, currentUser); 
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea eliminada exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/update")
    public String updateTask(
            @Valid @ModelAttribute Task task,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al actualizar tarea. Por favor, verifica los campos.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.task", bindingResult);
            redirectAttributes.addFlashAttribute("task", task);
            return "redirect:/tasks";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsernameEntity(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        try {
            taskService.updateTaskForUser(task.getId(), task, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea actualizada exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/tasks";
    }
}