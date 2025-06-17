package com.freedom.tareas.Repository;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Método para encontrar tareas por título y por user
    List<Task> findByTitleContainingIgnoreCaseAndUser(String title, User user);

    // Método para encontrar tareas por estado y por user, ordenadas por fecha de vencimiento
    List<Task> findByStatusAndUserOrderByDueDateAsc(String status, User user);
    
    // Método para encontrar todas las tareas de un user específico
    List<Task> findByUser(User user);

    // Método para encontrar una tarea por ID y que pertenezca a un user específico
    Optional<Task> findByIdAndUser(Long id, User user);

    // Método para encontrar tareas por user y por el campo activeOnPage
    List<Task> findByUserAndActiveOnPage(User user, String activeOnPage);

    List<Task> findByUser_Id(Long userId);

    List<Task> findByUser_IdAndActiveOnPage(Long userId, String activeOnPage);
    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);

    void deleteByUserId(Long userId); // Spring Data JPA lo implementará automáticamente si sigues la convención


}