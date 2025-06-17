package com.freedom.tareas.Repository;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitleContainingIgnoreCaseAndUser(String title, User user);

    List<Task> findByStatusAndUserOrderByDueDateAsc(String status, User user);
    
    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    List<Task> findByUserAndActiveOnPage(User user, String activeOnPage);

    List<Task> findByUser_Id(Long userId);

    List<Task> findByUser_IdAndActiveOnPage(Long userId, String activeOnPage);
    
    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);

    void deleteByUserId(Long userId);
}