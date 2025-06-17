package com.freedom.tareas.Controller;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.TaskService;
import com.freedom.tareas.Service.UserService;
import com.freedom.tareas.Service.UserService.UserNotFoundException; // Importa tu excepción personalizada

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Base URL for admin operations (Se mantiene así para el HTML y otras APIs)
@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN pueden acceder
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;

    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }
    
    @GetMapping
    public String adminPanel(Model model) {
        return "admin"; // Corresponde a admin.html
    }

    // Nuevo endpoint para obtener la lista de usuarios (API REST)
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // ENDPOINT EXISTENTE: Para obtener la información detallada de un usuario por su ID
    @GetMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<User> getUserDetails(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findUserById(userId);
        return userOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ENDPOINT EXISTENTE: Para obtener las tareas de un usuario por su ID
    @GetMapping("/api/users/{userId}/tasks")
    @ResponseBody
    public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
        List<Task> tasks = taskService.findTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    // NUEVO ENDPOINT: Para obtener una tarea específica de un usuario por su ID de tarea
    @GetMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Task> getTaskById(@PathVariable Long userId, @PathVariable Long taskId) {
        Optional<Task> taskOptional = taskService.findTaskByUserIdAndTaskId(userId, taskId);
        return taskOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // NUEVO ENDPOINT: Para actualizar una tarea específica de un usuario
    @PutMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Task> updateTask(@PathVariable Long userId, @PathVariable Long taskId,
                                           @RequestBody Task updatedTask) {
        try {
            Task result = taskService.updateTask(userId, taskId, updatedTask);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // NUEVO ENDPOINT: Para eliminar una tarea específica de un usuario
    @DeleteMapping("/api/users/{userId}/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Void> deleteTask(@PathVariable Long userId, @PathVariable Long taskId) {
        boolean deleted = taskService.deleteTask(userId, taskId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // **AÑADE ESTE NUEVO ENDPOINT para eliminar USUARIOS**
    @DeleteMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUserAndAssociatedTasks(userId);
            return ResponseEntity.noContent().build(); // 204 No Content para eliminación exitosa
        } catch (UserNotFoundException e) { // Usamos la excepción personalizada
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // **¡EL MÉTODO QUE ESTABA DANDO PROBLEMAS!**
    // Se corrige la ruta para que coincida con lo que envía el frontend: /admin/api/users/{userId}/assign-admin
    @PutMapping("/api/users/{userId}/assign-admin") // <--- ¡CAMBIO AQUÍ!
    @ResponseBody // Necesario para que devuelva una respuesta JSON/texto en lugar de un nombre de vista
    public ResponseEntity<?> assignAdminRole(@PathVariable Long userId) {
        try {
            userService.assignAdminRole(userId); // Llama a tu método de servicio para actualizar el rol del usuario
            return ResponseEntity.ok().build(); // Devuelve 200 OK
        } catch (UserNotFoundException e) { // Usa tu excepción personalizada
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Devuelve 404 con mensaje
        } catch (IllegalArgumentException e) { // Para el caso de que ya sea ADMIN
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Devuelve 400 con mensaje
        } catch (Exception e) {
            System.err.println("Error al asignar rol de administrador al usuario " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asignar rol de administrador: " + e.getMessage());
        }
    }
}