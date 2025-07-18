package com.freedom.tareas.dto;

import java.time.LocalDate;

public record AlertNavDTO(Long taskId,
        String title,
        LocalDate dueDate,
        boolean overdue) {
}