package com.freedom.tareas.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class TaskController {

    // Inyecta los servicios de tareas y usuarios.
    private final TaskService taskService;
    private final UserService userService;
    private final List<String> organizationTips = new ArrayList<>();

    // Constructor que inicializa servicios y carga consejos de organización.
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
        initializeTips();
    }

    // Carga una lista predefinida de consejos de organización al iniciar el controlador.
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

    // Muestra la página de inicio, incluyendo un resumen de tareas y un consejo aleatorio.
    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página principal.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username).orElse(null);
        model.addAttribute("user", currentUser);

        List<Task> allTasks = new ArrayList<>();
        if (currentUser != null) {
            // Obtiene solo tareas activas para el resumen.
            allTasks = taskService.obtenerTareasPorUsuario(currentUser); 
            System.out.println("LOG: Cargando resumen de tareas para el usuario: " + username);
        } else {
            System.out.println("LOG: Usuario no autenticado en la página principal.");
        }

        // Añade al modelo los recuentos de tareas para el panel de resumen.
        model.addAttribute("totalTasksCount", allTasks.size());
        model.addAttribute("completedTasksCount",
                allTasks.stream().filter(t -> "Completada".equalsIgnoreCase(t.getStatus())).count());
        model.addAttribute("pendingTasksCount",
                allTasks.stream().filter(t -> !"Completada".equalsIgnoreCase(t.getStatus())).count());
        
        model.addAttribute("currentUri", request.getRequestURI());

        // Obtiene y añade al modelo las tareas próximas a vencer.
        LocalDate today = LocalDate.now();
        LocalDate twoDaysFromNow = today.plusDays(2);
        List<Task> tasksDueSoon = taskService.obtenerTareasProximasPorUsuario(currentUser, today, twoDaysFromNow);
        model.addAttribute("tasksDueSoon", tasksDueSoon);

        // Selecciona y añade un consejo de organización aleatorio.
        Random random = new Random();
        if (!organizationTips.isEmpty()) {
            int randomIndex = random.nextInt(organizationTips.size());
            String randomTip = organizationTips.get(randomIndex);
            model.addAttribute("currentTip", randomTip);
            model.addAttribute("allTips", organizationTips); // También pasa todos los consejos.
        } else {
            model.addAttribute("currentTip", "No hay consejos disponibles.");
            model.addAttribute("allTips", new ArrayList<>());
        }
        return "index"; // Retorna la vista principal.
    }

    // Muestra la página con todas las tareas del usuario, con opciones de filtro y ordenación.
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
        System.out.println("LOG: Accediendo a la página de listado de tareas.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        model.addAttribute("user", currentUser);

        // Obtiene tareas filtradas y ordenadas utilizando el servicio.
        List<Task> filteredTasks = taskService.obtenerTareasFiltradasYOrdenadasPorUsuario(currentUser, search, statusFilter,
                priorityFilter, etiquetaFilter, sortBy, sortDir);
        System.out.println("LOG: Cargando tareas filtradas y ordenadas para el usuario: " + username);
        
        // Añade al modelo las tareas filtradas y los parámetros de filtro/ordenación.
        model.addAttribute("tasks", filteredTasks);
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("priorityFilter", priorityFilter);
        model.addAttribute("etiquetaFilter", etiquetaFilter);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "tasks"; // Retorna la vista de listado de tareas.
    }


    // Muestra la página del calendario.
    @GetMapping("/calendar")
    public String calendarPage(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página del calendario.");
        model.addAttribute("currentUri", request.getRequestURI());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        model.addAttribute("user", currentUser);
        return "calendar"; // Retorna la vista del calendario.
    }


    // Proporciona los datos de las tareas en formato JSON para el calendario.
    @GetMapping("/api/tasks-calendar")
    @ResponseBody // Indica que el retorno es directamente el cuerpo de la respuesta HTTP.
    public List<Map<String, Object>> getTasksForCalendar() {
        System.out.println("\n************************************************************************************");
        System.out.println("DEBUG JWT: Solicitud de API para obtener tareas del calendario.");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        // Obtiene todas las tareas activas del usuario.
        List<Task> allTasks = taskService.obtenerTareasPorUsuario(currentUser);
        
        System.out.println("DEBUG JWT: Tareas obtenidas para el calendario del usuario: " + username);
        System.out.println("************************************************************************************\n");
        
        // Mapea las tareas a un formato compatible con el calendario (FullCalendar).
        return allTasks.stream()
                .filter(task -> task.getDueDate() != null) // Solo incluye tareas con fecha de vencimiento.
                .map(task -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", task.getId());
                    event.put("title", task.getTitle());
                    event.put("start", task.getDueDate());
                    
                    // Asigna un color al evento según el estado o prioridad de la tarea.
                    String color = "#007bff"; // Color por defecto (azul).
                    if ("Completada".equalsIgnoreCase(task.getStatus())) {
                        color = "#28a745"; // Verde para completadas.
                    } else if ("Alta".equalsIgnoreCase(task.getPriority())) {
                        color = "#dc3545"; // Rojo para prioridad alta.
                    } else if ("Media".equalsIgnoreCase(task.getPriority())) {
                        color = "#ffc107"; // Amarillo para prioridad media.
                    }
                    event.put("color", color);

                    // Añade propiedades extendidas para detalles adicionales en el calendario.
                    Map<String, Object> extendedProps = new HashMap<>();
                    extendedProps.put("description", task.getDescription());
                    extendedProps.put("priority", task.getPriority());
                    extendedProps.put("status", task.getStatus());
                    extendedProps.put("etiqueta", task.getEtiqueta());
                    event.put("extendedProps", extendedProps);
                    return event;
                })
                .collect(Collectors.toList()); // Colecta los eventos en una lista.
    }

    // Muestra el formulario para que el usuario pueda crear una nueva tarea.
    @GetMapping("/tasks/add")
    public String showAddTaskForm(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo al formulario para añadir nueva tarea.");
        model.addAttribute("taskFormObject", new Task()); // Objeto Task vacío para el formulario.
        model.addAttribute("currentUri", request.getRequestURI());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        model.addAttribute("user", currentUser);
        return "add-task"; // Retorna la vista del formulario.
    }

    // Procesa el envío del formulario para guardar una nueva tarea.
    // Realiza validación y asocia la tarea al usuario actual.
    @PostMapping("/tasks/save")
    @SuppressWarnings("CallToPrintStackTrace") // Suprime la advertencia de printStackTrace.
    public String saveTask(@Valid @ModelAttribute("taskFormObject") Task task,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpServletRequest request) {
        System.out.println("LOG: Solicitud para guardar nueva tarea: " + task.getTitle());
        
        // Verifica si hay errores de validación.
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> System.out.println("ERROR -> " + error));
            System.err.println("LOG ERROR: Errores de validación al intentar guardar tarea.");
            model.addAttribute("currentUri", "/tasks/add");
            return "add-task"; // Si hay errores, regresa al formulario.
        }
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.buscarEntidadPorUsername(username)
                    .orElseThrow(() -> new RuntimeException(
                            "Usuario autenticado no encontrado en la base de datos: " + username));
            
            task.setUser(currentUser); // Asigna la tarea al usuario actual.
            // activeOnPage se establece a "on" por defecto en TaskService.crearTarea
            taskService.crearTarea(task); // Llama al servicio para crear la tarea.
            
            System.out.println("LOG: Tarea '" + task.getTitle() + "' agregada exitosamente para el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea agregada exitosamente!");
        } catch (Exception e) {
            e.printStackTrace(); // En un entorno de producción, usa un logger.
            System.err.println("LOG ERROR: Error al agregar tarea '" + task.getTitle() + "': " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar tarea: " + e.getMessage());
            model.addAttribute("currentUri", "/tasks/add");
            model.addAttribute("taskFormObject", task);
            return "add-task"; // En caso de error, regresa al formulario.
        }
        return "redirect:/tasks";
    }

    // Marca una tarea como "Completada" y actualiza su fecha de finalización.
    @PostMapping("/tasks/complete/{id}")
    public String completeTask(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para marcar tarea con ID: " + id + " como completada.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        try {
            // Llama al método en el servicio que actualiza el estado y la fecha de finalización.
            taskService.marcarTareaComoCompletada(id, currentUser); 
            System.out.println("LOG: Tarea con ID " + id + " marcada como completada exitosamente por el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea marcada como completada!");
            return "redirect:/tasks";
        } catch (RuntimeException e) {
            System.err.println("LOG ERROR: Error al marcar tarea con ID " + id + " como completada para el usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al marcar tarea como completada: " + e.getMessage());
            return "redirect:/tasks";
        }
    }

    // Mueve una tarea a la papelera (realiza un "soft delete").
    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para enviar tarea con ID: " + id + " a la papelera.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        try {
            taskService.enviarTareaAPapelera(id, currentUser);
            System.out.println("LOG: Tarea con ID " + id + " enviada a la papelera exitosamente por el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea enviada a la papelera exitosamente!");
        } catch (RuntimeException e) {
            System.err.println("LOG ERROR: Error al enviar tarea con ID " + id + " a la papelera para el usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar a papelera: " + e.getMessage());
        }
        return "redirect:/tasks"; 
    }

    // Procesa el envío del formulario para actualizar una tarea existente.
    @PostMapping("/tasks/update")
    public String updateTask(
            @Valid @ModelAttribute Task task,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        System.out.println("LOG: Solicitud para actualizar tarea con ID: " + task.getId());
        
        // Verifica si hay errores de validación.
        if (bindingResult.hasErrors()) {
            System.err.println("LOG ERROR: Errores de validación al intentar actualizar tarea con ID: " + task.getId());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al actualizar tarea. Por favor, verifica los campos.");
            // Añade los errores y el objeto tarea al modelo para mostrarlos en la vista de origen.
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.task", bindingResult);
            redirectAttributes.addFlashAttribute("task", task);
            return "redirect:/tasks";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        try {
            taskService.actualizarTareaPorUsuario(task.getId(), task, currentUser);
            System.out.println("LOG: Tarea con ID " + task.getId() + " actualizada exitosamente por el usuario: " + username);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tarea actualizada exitosamente!");
        } catch (RuntimeException e) {
            System.err.println("LOG ERROR: Error al actualizar tarea con ID " + task.getId() + " para el usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/tasks"; 
    }
}