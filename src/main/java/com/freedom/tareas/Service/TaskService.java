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
import com.freedom.tareas.Repository.UserRepository; // Necesitarás esto para buscar el User si no lo tienes ya

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
            task.setActiveOnPage("on"); // Por defecto activa
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
            existingTask.setEtiqueta(updatedTask.getEtiqueta()); // Asegúrate de que este campo existe en tu Task Model

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
            // Asumiendo un orden específico para prioridades (Ej: ALTA > MEDIA > BAJA)
            comparator = Comparator.comparing((Task task) -> {
                if (task.getPriority() == null) return 3; // Nulo al final
                return switch (task.getPriority().toUpperCase()) {
                    case "ALTA" -> 1;
                    case "MEDIA" -> 2;
                    case "BAJA" -> 3;
                    default -> 4; // Prioridades desconocidas al final
                };
            });
        } else if ("status".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Task::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }
        else {
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
                        task.getDueDate().isAfter(today.minusDays(1)) && // Incluye hoy
                        task.getDueDate().isBefore(twoDaysFromNow.plusDays(1))) // Incluye hasta dos días más
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    // Método de ejemplo para encontrar todas las tareas (se mantiene)
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    // Método de ejemplo para encontrar una tarea por ID (se mantiene)
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    // Método de ejemplo para guardar o actualizar una tarea (se mantiene)
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    // Método de ejemplo para eliminar una tarea (se mantiene)
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // --- Métodos NUEVOS / MODIFICADOS para el AdminController ---

    /**
     * Obtiene una lista de tareas activas asignadas a un usuario específico por su ID.
     * Este método es para el AdminController para mostrar las tareas de un usuario.
     *
     * @param userId El ID del usuario.
     * @return Una lista de tareas activas.
     */
    @Transactional(readOnly = true)
    public List<Task> findTasksByUserId(Long userId) {
        // Necesitas asegurarte de que tu TaskRepository tenga un método como findByUser_IdAndActiveOnPage
        // o si solo quieres todas las tareas, findByUser_Id.
        // Si quieres solo las activas, lo ideal es:
        return taskRepository.findByUser_IdAndActiveOnPage(userId, "on");
    }

    /**
     * Obtiene una tarea específica por su ID de tarea y el ID del usuario al que pertenece.
     * Importante para el modal de edición del administrador.
     *
     * @param userId El ID del usuario propietario de la tarea.
     * @param taskId El ID de la tarea a buscar.
     * @return Un Optional que contiene la tarea si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Task> findTaskByUserIdAndTaskId(Long userId, Long taskId) {
        // Asume que TaskRepository tiene un método como findByIdAndUser_Id
        return taskRepository.findByIdAndUser_Id(taskId, userId);
    }

    /**
     * Actualiza una tarea existente para un usuario específico desde la perspectiva del administrador.
     *
     * @param userId El ID del usuario propietario de la tarea.
     * @param taskId El ID de la tarea a actualizar.
     * @param updatedTask El objeto Task con los datos actualizados.
     * @return La tarea actualizada.
     * @throws IllegalArgumentException si la tarea no se encuentra o no pertenece al usuario.
     */
    @Transactional
    public Task updateTask(Long userId, Long taskId, @Valid Task updatedTask) {
        // 1. Verificar si la tarea existe y pertenece al usuario
        Optional<Task> existingTaskOptional = taskRepository.findByIdAndUser_Id(taskId, userId);

        if (existingTaskOptional.isPresent()) {
            Task existingTask = existingTaskOptional.get();

            // 2. Aplicar los cambios del updatedTask al existingTask
            // ¡Cuidado! Solo actualiza los campos que el administrador debe poder cambiar.
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDueDate(updatedTask.getDueDate());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setEtiqueta(updatedTask.getEtiqueta()); // Si el admin puede editar etiquetas

            // Lógica para manejar la fecha de completado basada en el cambio de estado
            if ("Completada".equalsIgnoreCase(updatedTask.getStatus()) && !"Completada".equalsIgnoreCase(existingTask.getStatus())) {
                existingTask.setCompletionDate(LocalDate.now());
            } else if (!"Completada".equalsIgnoreCase(updatedTask.getStatus()) && "Completada".equalsIgnoreCase(existingTask.getStatus())) {
                existingTask.setCompletionDate(null);
            }

            // Mantener activeOnPage como está, a menos que el admin tenga un control explícito para cambiarlo
            // existingTask.setActiveOnPage(updatedTask.getActiveOnPage()); // Descomentar si el admin puede "re-activar"

            // 3. Guardar la tarea actualizada
            return taskRepository.save(existingTask);
        } else {
            // Si la tarea no se encuentra o no pertenece al usuario
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + userId + " y Tarea ID: " + taskId);
        }
    }

    /**
     * Elimina una tarea de forma permanente de la base de datos,
     * asegurándose de que pertenece al usuario especificado.
     * Este es un "hard delete", a diferencia de `deleteTaskForUser` que es un "soft delete".
     *
     * @param userId El ID del usuario propietario de la tarea.
     * @param taskId El ID de la tarea a eliminar.
     * @return true si la tarea fue eliminada, false si no se encontró o no pertenecía al usuario.
     */
    @Transactional
    public boolean deleteTask(Long userId, Long taskId) {
        Optional<Task> taskToDeleteOptional = taskRepository.findByIdAndUser_Id(taskId, userId);
        if (taskToDeleteOptional.isPresent()) {
            taskRepository.delete(taskToDeleteOptional.get());
            return true;
        }
        return false; // Tarea no encontrada o no pertenece al usuario
    }
}