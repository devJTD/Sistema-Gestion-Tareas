package com.freedom.tareas.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;

import jakarta.validation.Valid; // Mantener si se usa en otros servicios, pero aquí no

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    // private final UserRepository userRepository; // Se puede quitar si no se usa en otros métodos

    // Constructor corregido para no inyectar UserRepository si no se usa
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task crearTarea(@Valid Task tarea) {
        System.out.println("LOG: Intentando crear tarea: '" + tarea.getTitle() + "' para usuario ID: " + (tarea.getUser() != null ? tarea.getUser().getId() : "N/A"));
        if (tarea.getUser() == null) {
            System.err.println("LOG ERROR: Fallo al crear tarea: la tarea no está asociada a un usuario.");
            throw new IllegalArgumentException("La tarea debe estar asociada a un usuario.");
        }
        // La fecha de completado NO se establece aquí al crear, se hará con el botón específico.
        // Asegurarse de que activeOnPage se establece a "on" por defecto al crear
        if (tarea.getActiveOnPage() == null || tarea.getActiveOnPage().isEmpty() || "off".equalsIgnoreCase(tarea.getActiveOnPage())) {
            tarea.setActiveOnPage("on");
            System.out.println("LOG: activeOnPage establecido a 'on' para la tarea ID: " + tarea.getId());
        }
        // Asegurarse de que deletedAt es null al crear una tarea
        tarea.setDeletedAt(null);
        Task savedTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea creada y guardada exitosamente con ID: " + savedTask.getId());
        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasPorUsuario(User usuario) {
        System.out.println("LOG: Obteniendo todas las tareas (incluyendo completadas) para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        // Obtener TODAS las tareas que NO estén en la papelera (activeOnPage="on")
        // Las tareas completadas también tienen activeOnPage="on"
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "on");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas activas (incluyendo completadas) para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        return tasks;
    }

    @Transactional(readOnly = true)
    public Optional<Task> obtenerTareaPorIdYUsuario(Long id, User usuario) {
        System.out.println("LOG: Buscando tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> task = taskRepository.findByIdAndUser(id, usuario)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage())); // Solo si está activa
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
            System.out.println("LOG: Tarea ID " + id + " encontrada.");

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            // No se actualiza activeOnPage ni deletedAt aquí, eso se maneja en eliminar/restaurar
            System.out.println("LOG: Datos de tarea ID " + id + " actualizados en memoria.");

            // **Lógica de completionDate removida de aquí**
            // Ahora se maneja con el nuevo método 'marcarTareaComoCompletada'

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + id + " guardada en la base de datos.");
            return updatedTask;
        } else {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para actualización.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
    }

    // NUEVO MÉTODO: Marcar una tarea como completada
    @Transactional
    public Task marcarTareaComoCompletada(Long id, User usuario) {
        System.out.println("LOG: Intentando marcar tarea con ID: " + id + " como completada para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para marcar como completada.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }

        Task tarea = tareaEncontrada.get();
        // Solo actualizar si no está ya completada para evitar actualizar la fecha innecesariamente
        if (!"Completada".equalsIgnoreCase(tarea.getStatus())) {
            tarea.setStatus("Completada");
            tarea.setCompletionDate(LocalDate.now());
            System.out.println("LOG: Tarea ID " + id + " marcada como 'Completada'. Fecha de completado establecida.");
        } else {
            System.out.println("LOG: Tarea ID " + id + " ya estaba completada. No se realizó ningún cambio.");
        }
        
        Task updatedTask = taskRepository.save(tarea);
        return updatedTask;
    }


    // Método modificado para "eliminar suavemente" (mover a papelera)
    @Transactional
    public void enviarTareaAPapelera(Long id, User usuario) {
        System.out.println("LOG: Intentando enviar tarea con ID: " + id + " a la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para enviar a papelera.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("off"); // Marcar como inactiva (en papelera)
        tarea.setDeletedAt(LocalDate.now()); // Establecer la fecha de eliminación
        taskRepository.save(tarea);
        System.out.println("LOG: Tarea ID " + id + " enviada a la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
    }

    // Nuevo método para restaurar una tarea desde la papelera
    @Transactional
    public Task restaurarTarea(Long id, User usuario) {
        System.out.println("LOG: Intentando restaurar tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para restaurar.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("on"); // Marcar como activa
        tarea.setDeletedAt(null); // Limpiar la fecha de eliminación
        Task restoredTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea ID " + id + " restaurada exitosamente para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        return restoredTask;
    }

    // Nuevo método para eliminar una tarea permanentemente
    @Transactional
    public void eliminarTareaPermanentemente(Long id, User usuario) {
        System.out.println("LOG: Intentando eliminar permanentemente tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para eliminación permanente.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        taskRepository.delete(tareaEncontrada.get());
        System.out.println("LOG: Tarea ID " + id + " eliminada permanentemente para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
    }

    // Nuevo método para obtener tareas en la papelera
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasEnPapelera(User usuario) {
        System.out.println("LOG: Obteniendo tareas en papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "off");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas en la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> obtenerTareasFiltradasYOrdenadasPorUsuario(User usuario, String busqueda, String filtroEstado,
                                                                 String filtroPrioridad, String filtroEtiqueta,
                                                                 String ordenarPor, String direccionOrden) {
        System.out.println("LOG: Obteniendo tareas filtradas y ordenadas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        System.out.println("LOG: Filtros - Búsqueda: '" + busqueda + "', Estado: '" + filtroEstado + "', Prioridad: '" + filtroPrioridad + "', Etiqueta: '" + filtroEtiqueta + "'");
        System.out.println("LOG: Ordenación - Por: '" + ordenarPor + "', Dirección: '" + direccionOrden + "'");

        // Solo obtener tareas que están "on" (no en la papelera), incluyendo completadas si activeOnPage="on"
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
        // Solo obtener tareas que están "on" (no en la papelera)
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

    // Métodos para administración (sin filtro activeOnPage explícito, pero se puede añadir si es necesario)
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

    // Este método ya no se usará para la eliminación suave, solo para eliminación física si fuera necesario
    public void eliminarTarea(Long id) {
        System.out.println("LOG: Eliminando tarea físicamente por ID: " + id);
        taskRepository.deleteById(id);
        System.out.println("LOG: Tarea ID " + id + " eliminada físicamente.");
    }

    @Transactional(readOnly = true)
    public List<Task> buscarTareasPorIdUsuario(Long idUsuario) {
        System.out.println("LOG: Buscando tareas activas por ID de usuario para admin: " + idUsuario);
        // Solo obtener tareas que están "on" (no en la papelera) para la vista de admin por defecto
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
            System.out.println("LOG: Tarea ID " + idTarea + " encontrada para admin.");

            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            // No se actualiza activeOnPage ni deletedAt aquí
            System.out.println("LOG: Datos de tarea ID " + idTarea + " actualizados en memoria (desde admin).");

            // **Lógica de completionDate removida de aquí para administración también**
            // Se asume que la fecha de completado la gestiona el flujo de usuario o se actualiza manualmente
            // si es un caso de uso de administrador. Por simplicidad, se alinea con el flujo de usuario.

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + idTarea + " guardada en la base de datos (desde admin).");
            return updatedTask;
        } else {
            System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: " + idUsuario + " para actualización (desde admin).");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + idUsuario + " y Tarea ID: " + idTarea);
        }
    }

    // Este método es para la eliminación física desde el admin
    @Transactional
    public boolean eliminarTareaDesdeAdmin(Long idUsuario, Long idTarea) {
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

    // Tarea programada para eliminar tareas de la papelera después de 20 días
    // Se ejecuta cada día a la 1 AM (hora local del servidor)
    @Scheduled(cron = "0 0 1 * * ?") // Segundos, Minutos, Horas, Día del mes, Mes, Día de la semana
    @Transactional
    public void eliminarTareasAntiguasDePapelera() {
        System.out.println("LOG: Ejecutando tarea programada para eliminar tareas antiguas de la papelera.");
        LocalDate veinteDiasAtras = LocalDate.now().minusDays(20);
        List<Task> tareasParaEliminar = taskRepository.findByActiveOnPageAndDeletedAtBefore("off", veinteDiasAtras);

        if (tareasParaEliminar.isEmpty()) {
            System.out.println("LOG: No se encontraron tareas para eliminar automáticamente de la papelera.");
            return;
        }

        System.out.println("LOG: Se encontraron " + tareasParaEliminar.size() + " tareas para eliminar automáticamente.");
        taskRepository.deleteAll(tareasParaEliminar);
        System.out.println("LOG: Tareas antiguas eliminadas de la papelera exitosamente.");
    }
}