package com.freedom.tareas.Controller;

import java.util.List;
import java.util.Optional;

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

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.TaskService;
import com.freedom.tareas.Service.UserService;
import com.freedom.tareas.Service.UserService.UserNotFoundException;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    // Inyecta los servicios de usuario y tareas.
    private final UserService userService;
    private final TaskService taskService;

    // Inicializa los servicios inyectados.
    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    // Muestra la página principal del panel de administración.
    @GetMapping
    public String mostrarPanelAdmin(Model model) {
        System.out.println("LOG: Accediendo al panel de administración.");
        return "admin";
    }

    // Lista todos los usuarios registrados en el sistema.
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> listarTodosLosUsuarios() {
        System.out.println("LOG: Solicitud para listar todos los usuarios.");
        List<User> usuarios = userService.buscarTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // Obtiene los detalles de un usuario específico por su ID.
    @GetMapping("/api/users/{idUsuario}")
    @ResponseBody
    public ResponseEntity<User> obtenerUsuarioPorId(@PathVariable Long idUsuario) {
        System.out.println("LOG: Solicitud para obtener usuario con ID: " + idUsuario);
        Optional<User> usuario = userService.buscarUsuarioPorId(idUsuario);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Elimina un usuario y todas sus tareas asociadas.
    @DeleteMapping("/api/users/{idUsuario}")
    @ResponseBody
    public ResponseEntity<Void> eliminarUsuarioPorId(@PathVariable Long idUsuario) {
        System.out.println("LOG: Solicitud para eliminar usuario con ID: " + idUsuario);
        try {
            userService.eliminarUsuarioYSusTareas(idUsuario);
            System.out.println("LOG: Usuario con ID " + idUsuario + " eliminado exitosamente.");
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            System.err.println("LOG ERROR: Intento de eliminar usuario no encontrado con ID: " + idUsuario);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("LOG ERROR: Error al eliminar usuario con ID " + idUsuario + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Asigna el rol de administrador a un usuario existente.
    @PutMapping("/api/users/{idUsuario}/asignar-admin")
    @ResponseBody
    public ResponseEntity<?> asignarRolAdministrador(@PathVariable Long idUsuario) {
        System.out.println("LOG: Solicitud para asignar rol ADMIN a usuario con ID: " + idUsuario);
        try {
            userService.asignarRolAdmin(idUsuario);
            System.out.println("LOG: Rol ADMIN asignado a usuario con ID " + idUsuario + " exitosamente.");
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            System.err.println("LOG ERROR: Intento de asignar rol ADMIN a usuario no encontrado con ID: " + idUsuario);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("LOG ERROR: Error de argumento al asignar rol ADMIN a usuario con ID " + idUsuario + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("LOG ERROR: Error inesperado al asignar rol de administrador al usuario " + idUsuario + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al asignar rol de administrador: " + e.getMessage());
        }
    }

    // Lista todas las tareas asociadas a un usuario específico.
    @GetMapping("/api/users/{idUsuario}/tasks")
    @ResponseBody
    public ResponseEntity<List<Task>> listarTareasPorUsuario(@PathVariable Long idUsuario) {
        System.out.println("LOG: Solicitud para listar tareas del usuario con ID: " + idUsuario);
        List<Task> tareas = taskService.buscarTareasPorIdUsuario(idUsuario);
        return ResponseEntity.ok(tareas);
    }

    // Obtiene los detalles de una tarea específica de un usuario.
    @GetMapping("/api/users/{idUsuario}/tasks/{idTarea}")
    @ResponseBody
    public ResponseEntity<Task> obtenerTareaPorIdYUsuario(@PathVariable Long idUsuario, @PathVariable Long idTarea) {
        System.out.println("LOG: Solicitud para obtener tarea con ID " + idTarea + " del usuario con ID: " + idUsuario);
        Optional<Task> tarea = taskService.buscarTareaPorIdUsuarioYIdTarea(idUsuario, idTarea);
        return tarea.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualiza los detalles de una tarea específica de un usuario.
    @PutMapping("/api/users/{idUsuario}/tasks/{idTarea}")
    @ResponseBody
    public ResponseEntity<Task> actualizarTareaPorUsuario(@PathVariable Long idUsuario,
                                                          @PathVariable Long idTarea,
                                                          @RequestBody Task tareaActualizada) {
        System.out.println("LOG: Solicitud para actualizar tarea con ID " + idTarea + " del usuario con ID: " + idUsuario);
        try {
            Task tarea = taskService.actualizarTarea(idUsuario, idTarea, tareaActualizada);
            System.out.println("LOG: Tarea con ID " + idTarea + " del usuario " + idUsuario + " actualizada exitosamente.");
            return ResponseEntity.ok(tarea);
        } catch (IllegalArgumentException e) {
            System.err.println("LOG ERROR: Tarea con ID " + idTarea + " o usuario " + idUsuario + " no encontrado para actualización.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("LOG ERROR: Error al actualizar tarea con ID " + idTarea + " del usuario " + idUsuario + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

