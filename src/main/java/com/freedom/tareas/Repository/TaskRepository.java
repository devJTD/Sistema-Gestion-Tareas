package com.freedom.tareas.Repository;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Busca una tarea por su ID y usuario
    Optional<Task> findByIdAndUser(Long id, User user);

    // Busca tareas de un usuario con un valor específico en el campo activeOnPage
    List<Task> findByUserAndActiveOnPage(User user, String activeOnPage);

    // Busca tareas por ID de usuario y valor del campo activeOnPage
    List<Task> findByUser_IdAndActiveOnPage(Long userId, String activeOnPage);

    // Busca una tarea por su ID y el ID del usuario asociado
    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);

    // Elimina todas las tareas asociadas al ID de un usuario
    void deleteByUserId(Long userId);
}
