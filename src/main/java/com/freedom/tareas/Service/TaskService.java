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
        if (tarea.getUser() == null) {
            throw new IllegalArgumentException("La tarea debe estar asociada a un usuario.");
        }
        if ("Completada".equalsIgnoreCase(tarea.getStatus()) && tarea.getCompletionDate() == null) {
            tarea.setCompletionDate(LocalDate.now());
        }
        if (tarea.getActiveOnPage() == null || tarea.getActiveOnPage().isEmpty()) {
            tarea.setActiveOnPage("on");
        }
        return taskRepository.save(tarea);
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasPorUsuario(User usuario) {
        return taskRepository.findByUserAndActiveOnPage(usuario, "on");
    }

    @Transactional(readOnly = true)
    public Optional<Task> obtenerTareaPorIdYUsuario(Long id, User usuario) {
        return taskRepository.findByIdAndUser(id, usuario)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage()));
    }

    @Transactional
    public Task actualizarTareaPorUsuario(Long id, @Valid Task tareaActualizada, User usuario) {
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();
            String estadoAnterior = tarea.getStatus();

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());

            if ("Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && !"Completada".equalsIgnoreCase(estadoAnterior)) {
                tarea.setCompletionDate(LocalDate.now());
            } else if (!"Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && "Completada".equalsIgnoreCase(estadoAnterior)) {
                tarea.setCompletionDate(null);
            }

            return taskRepository.save(tarea);
        } else {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
    }

    @Transactional
    public void eliminarTareaPorUsuario(Long id, User usuario) {
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("off");
        taskRepository.save(tarea);
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasFiltradasYOrdenadasPorUsuario(User usuario, String busqueda, String filtroEstado,
                                                                 String filtroPrioridad, String filtroEtiqueta,
                                                                 String ordenarPor, String direccionOrden) {
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

        return tareasDelUsuario.stream()
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
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasProximasPorUsuario(User usuario, LocalDate hoy, LocalDate dentroDeDosDias) {
        return taskRepository.findByUserAndActiveOnPage(usuario, "on").stream()
                .filter(tarea -> !"Completada".equalsIgnoreCase(tarea.getStatus()))
                .filter(tarea -> tarea.getDueDate() != null &&
                        tarea.getDueDate().isAfter(hoy.minusDays(1)) &&
                        tarea.getDueDate().isBefore(dentroDeDosDias.plusDays(1)))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Task> buscarTodasLasTareas() {
        return taskRepository.findAll();
    }

    public Optional<Task> buscarTareaPorId(Long id) {
        return taskRepository.findById(id);
    }

    public Task guardarTarea(Task tarea) {
        return taskRepository.save(tarea);
    }

    public void eliminarTarea(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> buscarTareasPorIdUsuario(Long idUsuario) {
        return taskRepository.findByUser_IdAndActiveOnPage(idUsuario, "on");
    }

    @Transactional(readOnly = true)
    public Optional<Task> buscarTareaPorIdUsuarioYIdTarea(Long idUsuario, Long idTarea) {
        return taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
    }

    @Transactional
    public Task actualizarTarea(Long idUsuario, Long idTarea, @Valid Task tareaActualizada) {
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());

            if ("Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && !"Completada".equalsIgnoreCase(tarea.getStatus())) {
                tarea.setCompletionDate(LocalDate.now());
            } else if (!"Completada".equalsIgnoreCase(tareaActualizada.getStatus()) && "Completada".equalsIgnoreCase(tarea.getStatus())) {
                tarea.setCompletionDate(null);
            }

            return taskRepository.save(tarea);
        } else {
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + idUsuario + " y Tarea ID: " + idTarea);
        }
    }

    @Transactional
    public boolean eliminarTarea(Long idUsuario, Long idTarea) {
        Optional<Task> tareaAEliminar = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        if (tareaAEliminar.isPresent()) {
            taskRepository.delete(tareaAEliminar.get());
            return true;
        }
        return false;
    }
}
