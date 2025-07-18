package com.freedom.tareas.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.dto.AlertNavDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public List<AlertNavDTO> buildAlertsFor(String username) {
        Long userId = getUserIdFromUsername(username);
        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findAllAlerts(userId, today.plusDays(1));
        log.info(">>> AlertService para {}: {} tareas encontradas", username, tasks.size());
        tasks.forEach(t -> log.info("   - {} vence {}", t.getTitle(), t.getDueDate()));

        List<AlertNavDTO> list = new ArrayList<>();
        for (Task t : tasks) {
            boolean overdue = !t.getDueDate().isAfter(today);
            list.add(new AlertNavDTO(t.getId(), t.getTitle(), t.getDueDate(), overdue));
        }
        return list;
    }

    private Long getUserIdFromUsername(String username) {
        return userService.buscarEntidadPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username))
                .getId();
    }
}