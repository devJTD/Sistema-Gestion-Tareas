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

import jakarta.validation.Valid;

@Service
public class TaskService {

    // Inyecta el repositorio de tareas para la interacción con la base de datos.
    private final TaskRepository taskRepository;

    // Constructor que permite a Spring inyectar TaskRepository.
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Crea una nueva tarea, asegurándose de que esté asociada a un usuario.
    @Transactional
    public Task crearTarea(@Valid Task tarea) {
<<<<<<< HEAD
        System.out.println("LOG: Intentando crear tarea: '" + tarea.getTitle() + "' para usuario ID: "
                + (tarea.getUser() != null ? tarea.getUser().getId() : "N/A"));
=======
        System.out.println("LOG: Intentando crear tarea: '" + tarea.getTitle() + "' para usuario ID: " + (tarea.getUser() != null ? tarea.getUser().getId() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        if (tarea.getUser() == null) {
            System.err.println("LOG ERROR: Fallo al crear tarea: la tarea no está asociada a un usuario.");
            throw new IllegalArgumentException("La tarea debe estar asociada a un usuario.");
        }
<<<<<<< HEAD
        if (tarea.getActiveOnPage() == null || tarea.getActiveOnPage().isEmpty()
                || "off".equalsIgnoreCase(tarea.getActiveOnPage())) {
=======
        if (tarea.getActiveOnPage() == null || tarea.getActiveOnPage().isEmpty() || "off".equalsIgnoreCase(tarea.getActiveOnPage())) {
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
            tarea.setActiveOnPage("on");
            System.out.println("LOG: activeOnPage establecido a 'on' para la tarea ID: " + tarea.getId());
        }
        tarea.setDeletedAt(null);
        Task savedTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea creada y guardada exitosamente con ID: " + savedTask.getId());
        return savedTask;
    }

    // Obtiene todas las tareas activas ("on") de un usuario específico.
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasPorUsuario(User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Obteniendo todas las tareas (incluyendo completadas) para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "on");
        System.out.println(
                "LOG: Se encontraron " + tasks.size() + " tareas activas (incluyendo completadas) para el usuario: "
                        + (usuario != null ? usuario.getUsername() : "N/A"));
=======
        System.out.println("LOG: Obteniendo todas las tareas (incluyendo completadas) para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "on");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas activas (incluyendo completadas) para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return tasks;
    }

    // Busca una tarea por su ID y el usuario, filtrando solo las que estén activas.
    @Transactional(readOnly = true)
    public Optional<Task> obtenerTareaPorIdYUsuario(Long id, User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Buscando tarea con ID: " + id + " para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> task = taskRepository.findByIdAndUser(id, usuario)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage()));
        System.out.println("LOG: Tarea ID " + id
                + (task.isPresent() ? " encontrada y activa." : " no encontrada o inactiva.") + " para el usuario.");
=======
        System.out.println("LOG: Buscando tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> task = taskRepository.findByIdAndUser(id, usuario)
                .filter(t -> "on".equalsIgnoreCase(t.getActiveOnPage()));
        System.out.println("LOG: Tarea ID " + id + (task.isPresent() ? " encontrada y activa." : " no encontrada o inactiva.") + " para el usuario.");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return task;
    }

    // Actualiza los detalles de una tarea específica para un usuario.
    @Transactional
    public Task actualizarTareaPorUsuario(Long id, @Valid Task tareaActualizada, User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Intentando actualizar tarea con ID: " + id + " para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
=======
        System.out.println("LOG: Intentando actualizar tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();
            System.out.println("LOG: Tarea ID " + id + " encontrada.");

            // Actualiza los campos de la tarea.
            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            System.out.println("LOG: Datos de tarea ID " + id + " actualizados en memoria.");

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + id + " guardada en la base de datos.");
            return updatedTask;
        } else {
<<<<<<< HEAD
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario "
                    + (usuario != null ? usuario.getUsername() : "N/A") + " para actualización.");
=======
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para actualización.");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
    }

    // Marca una tarea como completada y registra la fecha actual.
    @Transactional
    public Task marcarTareaComoCompletada(Long id, User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Intentando marcar tarea con ID: " + id + " como completada para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario "
                    + (usuario != null ? usuario.getUsername() : "N/A") + " para marcar como completada.");
=======
        System.out.println("LOG: Intentando marcar tarea con ID: " + id + " como completada para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);

        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para marcar como completada.");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }

        Task tarea = tareaEncontrada.get();
        if (!"Completada".equalsIgnoreCase(tarea.getStatus())) {
            tarea.setStatus("Completada");
            tarea.setCompletionDate(LocalDate.now()); // Establece la fecha de finalización.
            System.out.println("LOG: Tarea ID " + id + " marcada como 'Completada'. Fecha de completado establecida.");
        } else {
            System.out.println("LOG: Tarea ID " + id + " ya estaba completada. No se realizó ningún cambio.");
        }
<<<<<<< HEAD

=======
        
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        Task updatedTask = taskRepository.save(tarea);
        return updatedTask;
    }

<<<<<<< HEAD
    // Mueve una tarea a la "papelera" estableciendo 'activeOnPage' a "off" y
    // 'deletedAt' a la fecha actual.
    @Transactional
    public void enviarTareaAPapelera(Long id, User usuario) {
        System.out.println("LOG: Intentando enviar tarea con ID: " + id + " a la papelera para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario "
                    + (usuario != null ? usuario.getUsername() : "N/A") + " para enviar a papelera.");
=======
    // Mueve una tarea a la "papelera" estableciendo 'activeOnPage' a "off" y 'deletedAt' a la fecha actual.
    @Transactional
    public void enviarTareaAPapelera(Long id, User usuario) {
        System.out.println("LOG: Intentando enviar tarea con ID: " + id + " a la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para enviar a papelera.");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("off"); // Marca como inactiva.
        tarea.setDeletedAt(LocalDate.now()); // Registra la fecha de eliminación.
        taskRepository.save(tarea);
<<<<<<< HEAD
        System.out.println("LOG: Tarea ID " + id + " enviada a la papelera para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
    }

    // Restaura una tarea de la papelera, volviendo 'activeOnPage' a "on" y
    // 'deletedAt' a null.
    @Transactional
    public Task restaurarTarea(Long id, User usuario) {
        System.out.println("LOG: Intentando restaurar tarea con ID: " + id + " para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario "
                    + (usuario != null ? usuario.getUsername() : "N/A") + " para restaurar.");
=======
        System.out.println("LOG: Tarea ID " + id + " enviada a la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
    }

    // Restaura una tarea de la papelera, volviendo 'activeOnPage' a "on" y 'deletedAt' a null.
    @Transactional
    public Task restaurarTarea(Long id, User usuario) {
        System.out.println("LOG: Intentando restaurar tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para restaurar.");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        Task tarea = tareaEncontrada.get();
        tarea.setActiveOnPage("on"); // Marca como activa.
        tarea.setDeletedAt(null); // Borra la fecha de eliminación.
        Task restoredTask = taskRepository.save(tarea);
<<<<<<< HEAD
        System.out.println("LOG: Tarea ID " + id + " restaurada exitosamente para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
=======
        System.out.println("LOG: Tarea ID " + id + " restaurada exitosamente para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return restoredTask;
    }

    // Elimina una tarea de forma permanente de la base de datos.
    @Transactional
    public void eliminarTareaPermanentemente(Long id, User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Intentando eliminar permanentemente tarea con ID: " + id + " para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario "
                    + (usuario != null ? usuario.getUsername() : "N/A") + " para eliminación permanente.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        taskRepository.delete(tareaEncontrada.get());
        System.out.println("LOG: Tarea ID " + id + " eliminada permanentemente para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
=======
        System.out.println("LOG: Intentando eliminar permanentemente tarea con ID: " + id + " para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser(id, usuario);
        if (tareaEncontrada.isEmpty()) {
            System.err.println("LOG ERROR: Tarea ID " + id + " no encontrada o no pertenece al usuario " + (usuario != null ? usuario.getUsername() : "N/A") + " para eliminación permanente.");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + id);
        }
        taskRepository.delete(tareaEncontrada.get());
        System.out.println("LOG: Tarea ID " + id + " eliminada permanentemente para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
    }

    // Obtiene todas las tareas que están en la papelera para un usuario específico.
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasEnPapelera(User usuario) {
<<<<<<< HEAD
        System.out.println("LOG: Obteniendo tareas en papelera para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "off");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas en la papelera para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
=======
        System.out.println("LOG: Obteniendo tareas en papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        List<Task> tasks = taskRepository.findByUserAndActiveOnPage(usuario, "off");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas en la papelera para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return tasks;
    }

    // Obtiene tareas de un usuario aplicando filtros y ordenación dinámica.
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasFiltradasYOrdenadasPorUsuario(User usuario, String busqueda, String filtroEstado,
<<<<<<< HEAD
            String filtroPrioridad, String filtroEtiqueta,
            String ordenarPor, String direccionOrden) {
        System.out.println("LOG: Obteniendo tareas filtradas y ordenadas para el usuario: "
                + (usuario != null ? usuario.getUsername() : "N/A"));
        System.out.println("LOG: Filtros - Búsqueda: '" + busqueda + "', Estado: '" + filtroEstado + "', Prioridad: '"
                + filtroPrioridad + "', Etiqueta: '" + filtroEtiqueta + "'");
=======
                                                                  String filtroPrioridad, String filtroEtiqueta,
                                                                  String ordenarPor, String direccionOrden) {
        System.out.println("LOG: Obteniendo tareas filtradas y ordenadas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A"));
        System.out.println("LOG: Filtros - Búsqueda: '" + busqueda + "', Estado: '" + filtroEstado + "', Prioridad: '" + filtroPrioridad + "', Etiqueta: '" + filtroEtiqueta + "'");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        System.out.println("LOG: Ordenación - Por: '" + ordenarPor + "', Dirección: '" + direccionOrden + "'");

        List<Task> tareasDelUsuario = taskRepository.findByUserAndActiveOnPage(usuario, "on");

        // Define el comparador para ordenar las tareas.
        Comparator<Task> comparador;
        if ("id".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getId);
        } else if ("title".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("dueDate".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing(Task::getDueDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("priority".equalsIgnoreCase(ordenarPor)) {
            comparador = Comparator.comparing((Task tarea) -> {
<<<<<<< HEAD
                if (tarea.getPriority() == null)
                    return 3;
=======
                if (tarea.getPriority() == null) return 3;
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
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
            comparador = comparador.reversed(); // Invierte el orden si es descendente.
        }

        // Aplica filtros y ordenación usando Streams.
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
                .sorted(comparador) // Ordena la lista.
                .collect(Collectors.toList());
<<<<<<< HEAD
        System.out.println(
                "LOG: Se encontraron " + filteredAndSortedTasks.size() + " tareas después de filtrar y ordenar.");
        return filteredAndSortedTasks;
    }

    // Obtiene tareas de un usuario con fecha de vencimiento próxima (hoy + 2 días),
    // excluyendo las completadas.
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasProximasPorUsuario(User usuario, LocalDate hoy, LocalDate dentroDeDosDias) {
        System.out.println(
                "LOG: Obteniendo tareas próximas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A")
                        + " (Hoy: " + hoy + ", Próximos 2 días: " + dentroDeDosDias + ")");
=======
        System.out.println("LOG: Se encontraron " + filteredAndSortedTasks.size() + " tareas después de filtrar y ordenar.");
        return filteredAndSortedTasks;
    }

    // Obtiene tareas de un usuario con fecha de vencimiento próxima (hoy + 2 días), excluyendo las completadas.
    @Transactional(readOnly = true)
    public List<Task> obtenerTareasProximasPorUsuario(User usuario, LocalDate hoy, LocalDate dentroDeDosDias) {
        System.out.println("LOG: Obteniendo tareas próximas para el usuario: " + (usuario != null ? usuario.getUsername() : "N/A") + " (Hoy: " + hoy + ", Próximos 2 días: " + dentroDeDosDias + ")");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        List<Task> upcomingTasks = taskRepository.findByUserAndActiveOnPage(usuario, "on").stream()
                .filter(tarea -> !"Completada".equalsIgnoreCase(tarea.getStatus())) // Excluye tareas completadas.
                .filter(tarea -> tarea.getDueDate() != null &&
                        tarea.getDueDate().isAfter(hoy.minusDays(1)) &&
                        tarea.getDueDate().isBefore(dentroDeDosDias.plusDays(1))) // Filtra por fecha.
                .sorted(Comparator.comparing(Task::getDueDate)) // Ordena por fecha de vencimiento.
                .collect(Collectors.toList());
        System.out.println("LOG: Se encontraron " + upcomingTasks.size() + " tareas próximas para el usuario.");
        return upcomingTasks;
    }

<<<<<<< HEAD
    // Busca y retorna todas las tareas en la base de datos (para uso
    // administrativo).
=======
    // Busca y retorna todas las tareas en la base de datos (para uso administrativo).
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
    public List<Task> buscarTodasLasTareas() {
        System.out.println("LOG: Buscando todas las tareas (sin filtro de usuario).");
        List<Task> allTasks = taskRepository.findAll();
        System.out.println("LOG: Se encontraron " + allTasks.size() + " tareas en total.");
        return allTasks;
    }

    // Busca una tarea por su ID.
    public Optional<Task> buscarTareaPorId(Long id) {
        System.out.println("LOG: Buscando tarea por ID: " + id);
        Optional<Task> task = taskRepository.findById(id);
        System.out.println("LOG: Tarea ID " + id + (task.isPresent() ? " encontrada." : " no encontrada."));
        return task;
    }

<<<<<<< HEAD
    // Guarda una tarea en la base de datos (puede ser para creación o actualización
    // general).
=======
    // Guarda una tarea en la base de datos (puede ser para creación o actualización general).
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
    public Task guardarTarea(Task tarea) {
        System.out.println("LOG: Guardando tarea (método genérico): '" + tarea.getTitle() + "'");
        Task savedTask = taskRepository.save(tarea);
        System.out.println("LOG: Tarea guardada (método genérico) con ID: " + savedTask.getId());
        return savedTask;
    }

    // Elimina una tarea de forma permanente por su ID.
    public void eliminarTarea(Long id) {
        System.out.println("LOG: Eliminando tarea físicamente por ID: " + id);
        taskRepository.deleteById(id);
        System.out.println("LOG: Tarea ID " + id + " eliminada físicamente.");
    }

    // Busca tareas activas por el ID de un usuario (para uso administrativo).
    @Transactional(readOnly = true)
    public List<Task> buscarTareasPorIdUsuario(Long idUsuario) {
        System.out.println("LOG: Buscando tareas activas por ID de usuario para admin: " + idUsuario);
        List<Task> tasks = taskRepository.findByUser_IdAndActiveOnPage(idUsuario, "on");
        System.out.println("LOG: Se encontraron " + tasks.size() + " tareas activas para el usuario ID: " + idUsuario);
        return tasks;
    }

<<<<<<< HEAD
    // Busca una tarea específica por ID de usuario y ID de tarea (para uso
    // administrativo).
    @Transactional(readOnly = true)
    public Optional<Task> buscarTareaPorIdUsuarioYIdTarea(Long idUsuario, Long idTarea) {
        System.out.println(
                "LOG: Buscando tarea ID: " + idTarea + " para el usuario ID: " + idUsuario + " (desde admin).");
        Optional<Task> task = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        System.out.println("LOG: Tarea ID " + idTarea + (task.isPresent() ? " encontrada." : " no encontrada.")
                + " para usuario ID: " + idUsuario + ".");
        return task;
    }

    // Actualiza una tarea específica de un usuario (desde el panel de
    // administración).
    @Transactional
    public Task actualizarTarea(Long idUsuario, Long idTarea, @Valid Task tareaActualizada) {
        System.out.println("LOG: Intentando actualizar tarea ID: " + idTarea + " para usuario ID: " + idUsuario
                + " (desde admin).");
=======
    // Busca una tarea específica por ID de usuario y ID de tarea (para uso administrativo).
    @Transactional(readOnly = true)
    public Optional<Task> buscarTareaPorIdUsuarioYIdTarea(Long idUsuario, Long idTarea) {
        System.out.println("LOG: Buscando tarea ID: " + idTarea + " para el usuario ID: " + idUsuario + " (desde admin).");
        Optional<Task> task = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        System.out.println("LOG: Tarea ID " + idTarea + (task.isPresent() ? " encontrada." : " no encontrada.") + " para usuario ID: " + idUsuario + ".");
        return task;
    }

    // Actualiza una tarea específica de un usuario (desde el panel de administración).
    @Transactional
    public Task actualizarTarea(Long idUsuario, Long idTarea, @Valid Task tareaActualizada) {
        System.out.println("LOG: Intentando actualizar tarea ID: " + idTarea + " para usuario ID: " + idUsuario + " (desde admin).");
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        Optional<Task> tareaEncontrada = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);

        if (tareaEncontrada.isPresent()) {
            Task tarea = tareaEncontrada.get();
            System.out.println("LOG: Tarea ID " + idTarea + " encontrada para admin.");

            // Actualiza los campos de la tarea.
            tarea.setTitle(tareaActualizada.getTitle());
            tarea.setDescription(tareaActualizada.getDescription());
            tarea.setDueDate(tareaActualizada.getDueDate());
            tarea.setPriority(tareaActualizada.getPriority());
            tarea.setStatus(tareaActualizada.getStatus());
            tarea.setEtiqueta(tareaActualizada.getEtiqueta());
            System.out.println("LOG: Datos de tarea ID " + idTarea + " actualizados en memoria (desde admin).");

            Task updatedTask = taskRepository.save(tarea);
            System.out.println("LOG: Tarea ID " + idTarea + " guardada en la base de datos (desde admin).");
            return updatedTask;
        } else {
<<<<<<< HEAD
            System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: "
                    + idUsuario + " para actualización (desde admin).");
            throw new IllegalArgumentException(
                    "Tarea no encontrada o no pertenece al usuario con ID: " + idUsuario + " y Tarea ID: " + idTarea);
        }
    }

    // Elimina físicamente una tarea de un usuario (desde el panel de
    // administración).
    @Transactional
    public boolean eliminarTareaDesdeAdmin(Long idUsuario, Long idTarea) {
        System.out.println("LOG: Intentando eliminar tarea físicamente ID: " + idTarea + " del usuario ID: " + idUsuario
                + " (desde admin).");
        Optional<Task> tareaAEliminar = taskRepository.findByIdAndUser_Id(idTarea, idUsuario);
        if (tareaAEliminar.isPresent()) {
            taskRepository.delete(tareaAEliminar.get());
            System.out
                    .println("LOG: Tarea ID " + idTarea + " eliminada físicamente del usuario ID: " + idUsuario + ".");
            return true;
        }
        System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: "
                + idUsuario + " para eliminación (desde admin).");
        return false;
    }

    // Tarea programada para eliminar permanentemente tareas de la papelera con más
    // de 20 días.
=======
            System.err.println("LOG ERROR: Tarea ID " + idTarea + " no encontrada o no pertenece al usuario ID: " + idUsuario + " para actualización (desde admin).");
            throw new IllegalArgumentException("Tarea no encontrada o no pertenece al usuario con ID: " + idUsuario + " y Tarea ID: " + idTarea);
        }
    }

    // Elimina físicamente una tarea de un usuario (desde el panel de administración).
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

    // Tarea programada para eliminar permanentemente tareas de la papelera con más de 20 días.
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void eliminarTareasAntiguasDePapelera() {
        System.out.println("LOG: Ejecutando tarea programada para eliminar tareas antiguas de la papelera.");
        LocalDate veinteDiasAtras = LocalDate.now().minusDays(20); // Define la fecha límite de 20 días atrás.
<<<<<<< HEAD
        List<Task> tareasParaEliminar = taskRepository.findByActiveOnPageAndDeletedAtBefore("off", veinteDiasAtras); // Busca
                                                                                                                     // tareas
                                                                                                                     // en
                                                                                                                     // papelera
                                                                                                                     // antes
                                                                                                                     // de
                                                                                                                     // la
                                                                                                                     // fecha
                                                                                                                     // límite.
=======
        List<Task> tareasParaEliminar = taskRepository.findByActiveOnPageAndDeletedAtBefore("off", veinteDiasAtras); // Busca tareas en papelera antes de la fecha límite.
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2

        if (tareasParaEliminar.isEmpty()) {
            System.out.println("LOG: No se encontraron tareas para eliminar automáticamente de la papelera.");
            return;
        }

<<<<<<< HEAD
        System.out
                .println("LOG: Se encontraron " + tareasParaEliminar.size() + " tareas para eliminar automáticamente.");
        taskRepository.deleteAll(tareasParaEliminar); // Elimina todas las tareas encontradas.
        System.out.println("LOG: Tareas antiguas eliminadas de la papelera exitosamente.");
    }

    public Optional<Task> findByIdAndUser(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user);
    }
=======
        System.out.println("LOG: Se encontraron " + tareasParaEliminar.size() + " tareas para eliminar automáticamente.");
        taskRepository.deleteAll(tareasParaEliminar); // Elimina todas las tareas encontradas.
        System.out.println("LOG: Tareas antiguas eliminadas de la papelera exitosamente.");
    }
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
}