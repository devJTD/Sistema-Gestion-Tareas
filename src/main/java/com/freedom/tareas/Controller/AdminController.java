// Ejemplo en com.freedom.tareas.Controller.AdminController.java
package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.TaskService;
import com.freedom.tareas.Service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping; // Importar para @DeleteMapping
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;    // Importar para @PutMapping
import org.springframework.web.bind.annotation.RequestBody;   // Importar para @RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Base URL for admin operations
@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN pueden acceder
public class AdminController {

    private final UserService userService;
    private final TaskService taskService; // Ahora sí lo vamos a necesitar

    // Constructor actualizado para inyectar TaskService
    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }
    
    @GetMapping
    public String adminPanel(Model model) {
        // En un inicio, solo devolvemos la vista.
        // Los usuarios se cargarán vía AJAX.
        return "admin"; // Corresponde a admin.html
    }

    // Nuevo endpoint para obtener la lista de usuarios (API REST)
    @GetMapping("/api/users") // Este es el endpoint que el JS llamará
    @ResponseBody // Indica que el retorno es el cuerpo de la respuesta HTTP, no una vista
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers(); // Asume que tienes un método findAllUsers en UserService
        return ResponseEntity.ok(users);
    }

    // **ENDPOINT EXISTENTE:** Para obtener la información detallada de un usuario por su ID
    @GetMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<User> getUserDetails(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findUserById(userId);
        return userOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // **ENDPOINT EXISTENTE:** Para obtener las tareas de un usuario por su ID
    @GetMapping("/api/users/{userId}/tasks")
    @ResponseBody
    public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
        List<Task> tasks = taskService.findTasksByUserId(userId); // Asume que tienes este método en TaskService
        return ResponseEntity.ok(tasks);
    }


    // **NUEVO ENDPOINT:** Para obtener una tarea específica de un usuario por su ID de tarea
    // Llamado por el modal de edición para precargar los datos
    @GetMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Task> getTaskById(@PathVariable Long userId, @PathVariable Long taskId) {
        // Se asume que taskService.findTaskByUserIdAndTaskId busca la tarea para ese usuario
        Optional<Task> taskOptional = taskService.findTaskByUserIdAndTaskId(userId, taskId);
        return taskOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // **NUEVO ENDPOINT:** Para actualizar una tarea específica de un usuario
    // Llamado por el modal de edición al guardar cambios
    @PutMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Task> updateTask(@PathVariable Long userId, @PathVariable Long taskId,
                                           @RequestBody Task updatedTask) {
        // En tu TaskService, necesitarás un método para actualizar la tarea.
        // Es importante verificar que la tarea realmente pertenece a ese userId y taskId.
        try {
            Task result = taskService.updateTask(userId, taskId, updatedTask);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Maneja casos donde la tarea no se encuentra o no pertenece al usuario
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Otros errores
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // **NUEVO ENDPOINT:** Para eliminar una tarea específica de un usuario
    // Llamado por el modal de confirmación al eliminar
    @DeleteMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Void> deleteTask(@PathVariable Long userId, @PathVariable Long taskId) {
        // En tu TaskService, necesitarás un método para eliminar la tarea.
        // Asegúrate de verificar que la tarea pertenece a este usuario antes de eliminarla.
        boolean deleted = taskService.deleteTask(userId, taskId); // Método que devuelve true si se elimina, false si no se encuentra o no pertenece al usuario
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content para eliminación exitosa
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found si la tarea no existe o no pertenece al usuario
        }
    }
}