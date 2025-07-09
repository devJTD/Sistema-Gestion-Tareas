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
    public Task crearTarea(@Valid Task tarea) {
        System.out.println("LOG: Intentando crear tarea: '" + tarea.getTitle() + "' para usuario ID: " + (tarea.getUser() != null ? tarea.getUser().getId() : "N/A"));
        if (tarea.getUser() == null) {
            System.err.println("LOG ERROR: Fallo al crear tarea: la tarea no está asociada a un usuario.");
            throw new IllegalArgumentException("La tarea debe estar asociada a un usuario.");
        }
        if ("Completada".equalsIgnoreCase(tarea.getStatus()) && tarea.getCompletionDate() == null) {
            tarea.setCompletionDate(LocalDate.now());
            System.out.println("LOG: Fecha de completado establecida para la tarea ID: " + tarea.getId());
        }
        if (tarea.getActiveOnPage() == null || tarea.getActiveOnPage().isEmpty()) {
            tarea.setActiveOnPage("on");
            System.out.println("LOG: activeOnPage establecido a 'on' para la tarea ID: " + tarea.getId());
        }
        Task savedTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea creada y guardada exitosamente con ID: " + savedTask.getId());
        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasPorUsuario(User usuario) {
        System.out.println("LOG: Obteniendo tareas activas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "on");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas activas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        return tasks;
    }

    @Transactional(readOnly = true)
    public Optional<Task> obtenerTareaPorIdYUsuario(Long id, User usuario) {
        System.out.println("LOG: Buscando tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> task = taskRepository.findByIdAndUser(id, usuario)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage()));
        if (task.isPresent()) {
            System.out.println("LOG: Tarea ID " + id + " encontrada y activa para el usuario.");
        } else {
            System.out.println("LOG: Tarea ID " + id + " no encontrada o inactiva para el usuario.");
        }
        return task;
    }

    @Transactional
    public Task actualizarTareaPorUsuario(Long id, @Valid Task tareaActualizada, User usuario) {
        System.out.println("LOG: Intentando actualizar tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();
            String estadoAnterior = tarea.getStatus();
            System.out.println("LOG: Tarea ID " + id + " encontrada. Estado anterior: " + estadoAnterior);

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            System.out.println("LOG: Datos de tarea ID " + id + " actualizados en memoria.");

            if ("Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && !"Completada".equalsIgnoreCase(estadoAnterior)) {
                tarea.setCompletionDate(LocalDate.now());
                System.out.println("LOG: Tarea ID " + id + " marcada como completada. Fecha de completado establecida.");
            } else if (!"Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && "Completada".equalsIgnoreCase(estadoAnterior)) {
                tarea.setCompletionDate(null);
                System.out.println("LOG: Tarea ID " + id + " desmarcada como completada. Fecha de completado eliminada.");
            }

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + id + " guardada en la base de datos.");
            return updatedTask;
        } else {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para actualización.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
    }

    @Transactional
    public void eliminarTareaPorUsuario(Long id, User usuario) {
        System.out.println("LOG: Intentando 'desactivar' tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para 'desactivación'.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("off");
        taskRepository.save(tarea);
        System.out.println("LOG: Tarea ID " + id + " marcada como inactiva para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasFiltradasYOrdenadasPorUsuario(User usuario, String busqueda, String filtroEstado,
                                                                  String filtroPrioridad, String filtroEtiqueta,
                                                                  String ordenarPor, String direccionOrden) {
        System.out.println("LOG: Obteniendo tareas filtradas y ordenadas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        System.out.println("LOG: Filtros - Búsqueda: '" + busqueda + "', Estado: '" + filtroEstado + "', Prioridad: '" + filtroPrioridad + "', Etiqueta: '" + filtroEtiqueta + "'");
        System.out.println("LOG: Ordenación - Por: '" + ordenarPor + "', Dirección: '" + direccionOrden + "'");

        List<Task> tareasDelUsuario = taskRepository.findByUserAndActiveOnPage(usuario, "on");

        Comparator<Task> comparador;
        if ("id".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getId);
        } else if ("title".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("dueDate".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getDueDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("priority".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing((Task tarea) -> {
                if (tarea.getPriority() == null) return 3;
                return switch (tarea.getPriority().toUpperCase()) {
                    case "ALTA" -> 1;
                    case "MEDIA" -> 2;
                    case "BAJA" -> 3;
                    default -> 4;
                };
            });
        } else if ("status".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else {
            comparador = Comparator.comparing(Task::getId);
        }

        if ("desc".equalsIgnoreCase(direccionOrden)) {
            comparador = comparador.reversed();
        }

        List<Task> filteredAndSortedTasks = tareasDelUsuario.stream()
                .filter(tarea -> {
                    boolean coincide = true;
                    if (busqueda != null && !busqueda.isEmpty()) {
                        coincide = coincide && tarea.getTitle().toLowerCase().contains(busqueda.toLowerCase());
                    }
                    if (filtroEstado != null && !filtroEstado.isEmpty()) {
                        coincide = coincide && filtroEstado.equalsIgnoreCase(tarea.getStatus());
                    }
                    if (filtroPrioridad != null && !filtroPrioridad.isEmpty()) {
                        coincide = coincide && filtroPrioridad.equalsIgnoreCase(tarea.getPriority());
                    }
                    if (filtroEtiqueta != null && !filtroEtiqueta.isEmpty()) {
                        coincide = coincide && (tarea.getEtiqueta() != null &&
                                tarea.getEtiqueta().toLowerCase().contains(filtroEtiqueta.toLowerCase()));
                    }
                    return coincide;
                })
                .sorted(comparador)
                .collect(Collectors.toList());
        System.out.println("LOG: Se encontraron " + filteredAndSortedTasks.size() + " tareas después de filtrar y ordenar.");
        return filteredAndSortedTasks;
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasProximasPorUsuario(User usuario, LocalDate hoy, LocalDate dentroDeDosDias) {
        System.out.println("LOG: Obteniendo tareas próximas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A") + " (Hoy: " + hoy + ", Próximos 2 días: " + dentroDeDosDias + ")");
        List<Task> upcomingTasks = taskRepository.findByUserAndActiveOnPage(usuario, "on").stream()
                .filter(tarea -> !"Completada".equalsIgnoreCase(tarea.getStatus()))
                .filter(tarea -> tarea.getDueDate() != null &&
                        tarea.getDueDate().isAfter(hoy.minusDays(1)) &&
                        tarea.getDueDate().isBefore(dentroDeDosDias.plusDays(1)))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
        System.out.println("LOG: Se encontraron " + upcomingTasks.size() + " tareas próximas para el usuario.");
        return upcomingTasks;
    }

    public List<Task> buscarTodasLasTareas() {
        System.out.println("LOG: Buscando todas las tareas (sin filtro de usuario).");
        List<Task> allTasks = taskRepository.findAll();
        System.out.println("LOG: Se encontraron " + allTasks.size() + " tareas en total.");
        return allTasks;
    }

    public Optional<Task> buscarTareaPorId(Long id) {
        System.out.println("LOG: Buscando tarea por ID: " + id);
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            System.out.println("LOG: Tarea ID " + id + " encontrada.");
        } else {
            System.out.println("LOG: Tarea ID " + id + " no encontrada.");
        }
        return task;
    }

    public Task guardarTarea(Task tarea) {
        System.out.println("LOG: Guardando tarea (método genérico): '" + tarea.getTitle() + "'");
        Task savedTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea guardada (método genérico) con ID: " + savedTask.getId());
        return savedTask;
    }

    public void eliminarTarea(Long id) {
        System.out.println("LOG: Eliminando tarea físicamente por ID: " + id);
        taskRepository.deleteById(id);
        System.out.println("LOG: Tarea ID " + id + " eliminada físicamente.");
    }

    @Transactional(readOnly = true)
    public List<Task> buscarTareasPorIdUsuario(Long idUsuario) {
        System.out.println("LOG: Buscando tareas activas por ID de usuario para admin: " + idUsuario);
        List<Task> tasks = taskRepository.findByUser_IdAndActiveOnPage(idUsuario, "on");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas activas para el usuario ID: " + idUsuario);
        return tasks;
    }

    @Transactional(readOnly = true)
    public Optional<Task> buscarTareaPorIdUsuarioYIdTarea(Long idUsuario, Long idTarea) {
        System.out.println("LOG: Buscando tarea ID: " + idTarea + " para el usuario ID: " + idUsuario + " (desde admin).");
        Optional<Task> task = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        if (task.isPresent()) {
            System.out.println("LOG: Tarea ID " + idTarea + " encontrada para usuario ID: " + idUsuario + ".");
        } else {
            System.out.println("LOG: Tarea ID " + idTarea + " no encontrada para usuario ID: " + idUsuario + ".");
        }
        return task;
    }

    @Transactional
    public Task actualizarTarea(Long idUsuario, Long idTarea, @Valid Task tareaActualizada) {
        System.out.println("LOG: Intentando actualizar tarea ID: " + idTarea + " para usuario ID: " + idUsuario + " (desde admin).");
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();
            String estadoAnterior = tarea.getStatus();
            System.out.println("LOG: Tarea ID " + idTarea + " encontrada para admin. Estado anterior: " + estadoAnterior);

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            System.out.println("LOG: Datos de tarea ID " + idTarea + " actualizados en memoria (desde admin).");

            if ("Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && !"Completada".equalsIgnoreCase(tarea.getStatus())) {
                tarea.setCompletionDate(LocalDate.now());
                System.out.println("LOG: Tarea ID " + idTarea + " marcada como completada (desde admin). Fecha de completado establecida.");
            } else if (!"Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && "Completada".equalsIgnoreCase(tarea.getStatus())) {
                tarea.setCompletionDate(null);
                System.out.println("LOG: Tarea ID " + idTarea + " desmarcada como completada (desde admin). Fecha de completado eliminada.");
            }

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + idTarea + " guardada en la base de datos (desde admin).");
            return updatedTask;
        } else {
            System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: " + idUsuario + " para actualización (desde admin).");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + idUsuario + " y Tarea ID: " + idTarea);
        }
    }

    @Transactional
    public boolean eliminarTarea(Long idUsuario, Long idTarea) {
        System.out.println("LOG: Intentando eliminar tarea físicamente ID: " + idTarea + " del usuario ID: " + idUsuario + " (desde admin).");
        Optional<Task> tareaAEliminar = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        if (tareaAEliminar.isPresent()) {
            taskRepository.delete(tareaAEliminar.get());
            System.out.println("LOG: Tarea ID " + idTarea + " eliminada físicamente del usuario ID: " + idUsuario + ".");
            return true;
        }
        System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: " + idUsuario + " para eliminación (desde admin).");
        return false;
    }
}
