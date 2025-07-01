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

    private final UserService userService;
    private final TaskService taskService;

    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    // === PANEL PRINCIPAL ===
    @GetMapping
    public String mostrarPanelAdmin(Model model) {
        return "admin";
    }

    // === GESTIÓN DE USUARIOS ===

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> listarTodosLosUsuarios() {
        List<User> usuarios = userService.buscarTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/api/users/{idUsuario}")
    @ResponseBody
    public ResponseEntity<User> obtenerUsuarioPorId(@PathVariable Long idUsuario) {
        Optional<User> usuario = userService.buscarUsuarioPorId(idUsuario);
        return usuario.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/users/{idUsuario}")
    @ResponseBody
    public ResponseEntity<Void> eliminarUsuarioPorId(@PathVariable Long idUsuario) {
        try {
            userService.eliminarUsuarioYSusTareas(idUsuario);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/api/users/{idUsuario}/asignar-admin")
    @ResponseBody
    public ResponseEntity<?> asignarRolAdministrador(@PathVariable Long idUsuario) {
        try {
            userService.asignarRolAdmin(idUsuario);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al asignar rol de administrador al usuario " + idUsuario + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al asignar rol de administrador: " + e.getMessage());
        }
    }

    // === GESTIÓN DE TAREAS POR USUARIO ===

    @GetMapping("/api/users/{idUsuario}/tasks")
    @ResponseBody
    public ResponseEntity<List<Task>> listarTareasPorUsuario(@PathVariable Long idUsuario) {
        List<Task> tareas = taskService.buscarTareasPorIdUsuario(idUsuario);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/api/users/{idUsuario}/tasks/{idTarea}")
    @ResponseBody
    public ResponseEntity<Task> obtenerTareaPorIdYUsuario(@PathVariable Long idUsuario, @PathVariable Long idTarea) {
        Optional<Task> tarea = taskService.buscarTareaPorIdUsuarioYIdTarea(idUsuario, idTarea);
        return tarea.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/api/users/{idUsuario}/tasks/{idTarea}")
    @ResponseBody
    public ResponseEntity<Task> actualizarTareaPorUsuario(@PathVariable Long idUsuario,
                                                          @PathVariable Long idTarea,
                                                          @RequestBody Task tareaActualizada) {
        try {
            Task tarea = taskService.actualizarTarea(idUsuario, idTarea, tareaActualizada);
            return ResponseEntity.ok(tarea);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/api/users/{idUsuario}/tasks/{idTarea}")
    @ResponseBody
    public ResponseEntity<Void> eliminarTareaPorUsuario(@PathVariable Long idUsuario, @PathVariable Long idTarea) {
        boolean eliminado = taskService.eliminarTarea(idUsuario, idTarea);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
