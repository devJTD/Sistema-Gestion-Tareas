package com.freedom.tareas.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;

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

    // Busca tareas inactivas que fueron eliminadas antes o en una fecha específica
    List<Task> findByActiveOnPageAndDeletedAtBefore(String activeOnPage, LocalDate date);

    // Busca una tarea por su ID y el usuario, para operaciones cuando NO está en la papelera (activeOnPage = "on")
    Optional<Task> findByIdAndUserAndActiveOnPage(Long id, User user, String activeOnPage);

    // Busca una tarea por su ID, el usuario, y que esté en la papelera (activeOnPage = "trash")
    Optional<Task> findByIdAndUserAndActiveOnPageAndDeletedAtIsNotNull(Long id, User user, String activeOnPage);

    // Busca todas las tareas que estén en la papelera (activeOnPage = "trash"), para la limpieza automática
    List<Task> findByActiveOnPage(String activeOnPage);
}