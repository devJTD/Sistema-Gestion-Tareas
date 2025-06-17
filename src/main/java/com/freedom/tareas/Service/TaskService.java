package com.freedom.tareas.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.Repository.UserRepository;

import jakarta.validation.Valid;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task createTask(@Valid Task task) {
        if (task.getUser() == null) {
            throw new IllegalArgumentException("La tarea debe estar asociada a un usuario.");
        }
        if ("Completada".equalsIgnoreCase(task.getStatus()) && task.getCompletionDate() == null) {
            task.setCompletionDate(LocalDate.now());
        }
        if (task.getActiveOnPage() == null || task.getActiveOnPage().isEmpty()) {
            task.setActiveOnPage("on");
        }
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByUserAndActiveOnPage(user, "on");
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskByIdAndUser(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage()));
    }

    @Transactional
    public Task updateTaskForUser(Long id, @Valid Task updatedTask, User user) {
        Optional<Task> existingTaskOptional = taskRepository.findByIdAndUser(id, user);

        if (existingTaskOptional.isPresent()) {
            Task existingTask = existingTaskOptional.get();

            String oldStatus = existingTask.getStatus();

            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDueDate(updatedTask.getDueDate());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setEtiqueta(updatedTask.getEtiqueta());

            if ("Completada".equalsIgnoreCase(updatedTask.getStatus()) && !"Completada".equalsIgnoreCase(oldStatus)) {
                existingTask.setCompletionDate(LocalDate.now());
            } else if (!"Completada".equalsIgnoreCase(updatedTask.getStatus())
                    && "Completada".equalsIgnoreCase(oldStatus)) {
                existingTask.setCompletionDate(null);
            }

            return taskRepository.save(existingTask);
        } else {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
    }

    @Transactional
    public void deleteTaskForUser(Long id, User user) {
        Optional<Task> taskToSoftDelete = taskRepository.findByIdAndUser(id, user);
        if (taskToSoftDelete.isEmpty()) {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task task = taskToSoftDelete.get();
        task.setActiveOnPage("off");
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getFilteredAndSortedTasksByUser(User user, String search, String statusFilter, String priorityFilter,
                                                      String etiquetaFilter, String sortBy, String sortDir) {
        List<Task> userTasks = taskRepository.findByUserAndActiveOnPage(user, "on");

        Comparator<Task> comparator;
        if ("id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Task::getId);
        } else if ("title".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Task::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("dueDate".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Task::getDueDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("priority".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing((Task task) -> {
                if (task.getPriority() == null) return 3;
                return switch (task.getPriority().toUpperCase()) {
                    case "ALTA" -> 1;
                    case "MEDIA" -> 2;
                    case "BAJA" -> 3;
                    default -> 4;
                };
            });
        } else if ("status".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Task::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else {
            comparator = Comparator.comparing(Task::getId);
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        return userTasks.stream()
                .filter(task -> {
                    boolean matches = true;

                    if (search != null && !search.isEmpty()) {
                        matches = matches && task.getTitle().toLowerCase().contains(search.toLowerCase());
                    }
                    if (statusFilter != null && !statusFilter.isEmpty()) {
                        matches = matches && statusFilter.equalsIgnoreCase(task.getStatus());
                    }
                    if (priorityFilter != null && !priorityFilter.isEmpty()) {
                        matches = matches && priorityFilter.equalsIgnoreCase(task.getPriority());
                    }
                    if (etiquetaFilter != null && !etiquetaFilter.isEmpty()) {
                        matches = matches && (task.getEtiqueta() != null
                                && task.getEtiqueta().toLowerCase().contains(etiquetaFilter.toLowerCase()));
                    }
                    return matches;
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Task> getDueSoonTasksByUser(User user, LocalDate today, LocalDate twoDaysFromNow) {
        return taskRepository.findByUserAndActiveOnPage(user, "on").stream()
                .filter(task -> !"Completada".equalsIgnoreCase(task.getStatus()))
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isAfter(today.minusDays(1)) &&
                        task.getDueDate().isBefore(twoDaysFromNow.plusDays(1)))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> findTasksByUserId(Long userId) {
        return taskRepository.findByUser_IdAndActiveOnPage(userId, "on");
    }

    @Transactional(readOnly = true)
    public Optional<Task> findTaskByUserIdAndTaskId(Long userId, Long taskId) {
        return taskRepository.findByIdAndUser_Id(taskId, userId);
    }

    @Transactional
    public Task updateTask(Long userId, Long taskId, @Valid Task updatedTask) {
        Optional<Task> existingTaskOptional = taskRepository.findByIdAndUser_Id(taskId, userId);

        if (existingTaskOptional.isPresent()) {
            Task existingTask = existingTaskOptional.get();

            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDueDate(updatedTask.getDueDate());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setEtiqueta(updatedTask.getEtiqueta());

            if ("Completada".equalsIgnoreCase(updatedTask.getStatus()) && !"Completada".equalsIgnoreCase(existingTask.getStatus())) {
                existingTask.setCompletionDate(LocalDate.now());
            } else if (!"Completada".equalsIgnoreCase(updatedTask.getStatus()) && "Completada".equalsIgnoreCase(existingTask.getStatus())) {
                existingTask.setCompletionDate(null);
            }

            return taskRepository.save(existingTask);
        } else {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + userId + " y Tarea ID: " + taskId);
        }
    }

    @Transactional
    public boolean deleteTask(Long userId, Long taskId) {
        Optional<Task> taskToDeleteOptional = taskRepository.findByIdAndUser_Id(taskId, userId);
        if (taskToDeleteOptional.isPresent()) {
            taskRepository.delete(taskToDeleteOptional.get());
            return true;
        }
        return false;
    }
}