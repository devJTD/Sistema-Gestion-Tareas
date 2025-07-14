package com.freedom.tareas.Model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks") 
public class Task {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "El título no puede estar vacío.")
    @Size(min = 1, max = 100, message = "El título debe tener entre 3 y 100 caracteres.")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres.")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "La fecha de vencimiento es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotBlank(message = "La prioridad no puede estar vacía.")
    @Column(name = "priority", nullable = false, length = 50)
    private String priority;

    @NotBlank(message = "El estado no puede estar vacío.")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Size(max = 50, message = "La etiqueta no puede exceder los 50 caracteres.")
    @Column(name = "tag", length = 50)
    private String etiqueta;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "active_on_page")
    private String activeOnPage;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    public Task(User user, String title, String description, LocalDate dueDate, String priority, String status,
            String activeOnPage) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.activeOnPage = "on";
    }
}