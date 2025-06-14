package com.freedom.tareas.Repository;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Método para encontrar tareas por título y por usuario
    List<Task> findByTitleContainingIgnoreCaseAndUsuario(String title, Usuario usuario);

    // Método para encontrar tareas por estado y por usuario, ordenadas por fecha de vencimiento
    List<Task> findByStatusAndUsuarioOrderByDueDateAsc(String status, Usuario usuario);
    
    // Método para encontrar todas las tareas de un usuario específico
    List<Task> findByUsuario(Usuario usuario);

    // Método para encontrar una tarea por ID y que pertenezca a un usuario específico
    Optional<Task> findByIdAndUsuario(Long id, Usuario usuario);

    // Método para encontrar tareas por usuario y por el campo activeOnPage
    List<Task> findByUsuarioAndActiveOnPage(Usuario usuario, String activeOnPage);
}