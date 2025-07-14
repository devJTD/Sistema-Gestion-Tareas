$(document).ready(function () {
    // Función para obtener el token JWT desde localStorage
    function getJwtToken() {
        return localStorage.getItem('jwtToken');
    }

    /**
     * Prepara el modal de edición de tareas con los datos proporcionados.
     * Se ha ajustado para no mostrar "Completada" en el selector de estado,
     * ya que ahora hay un botón específico para marcar como completada en la vista de lista.
     * Para el calendario, esto asegura consistencia.
     */
    function prepareEditTaskFormModal(
        id,
        title,
        description,
        dueDate,
        priority,
        status,
        etiqueta
    ) {
        $("#editTaskModalLabel").html('<i class="fas fa-edit"></i> Editar Tarea');
        $("#editTaskId").val(id);
        $("#editTaskTitle").val(title);
        $("#editTaskDescription").val(description);
        $("#editTaskDueDate").val(dueDate);
        $("#editTaskPriority").val(priority);

        // --- INICIO DE MODIFICACIÓN CLAVE PARA ELIMINAR OPCIÓN 'COMPLETADA' ---
        var $editTaskStatus = $("#editTaskStatus");
        $editTaskStatus.empty(); // Limpiar opciones existentes
        $editTaskStatus.append('<option value="Pendiente">Pendiente</option>');
        $editTaskStatus.append('<option value="En Proceso">En Proceso</option>');
        // Si la tarea ya está completada, se establece en "Pendiente" por defecto en el editor,
        // ya que la acción de "Completada" debe hacerse a través del botón específico en la vista de tareas.
        if (status === 'Completada') {
            $editTaskStatus.val('Pendiente');
        } else {
            $editTaskStatus.val(status);
        }
        // --- FIN DE MODIFICACIÓN CLAVE ---
        
        $("#editTaskEtiqueta").val(etiqueta);
    }

    function prepareDeleteTaskModal(taskId, taskTitle) {
        $("#taskTitleToDeleteModal").text(taskTitle);
        var deleteUrl = "/tasks/delete/";
        $("#deleteTaskFormInModal").attr("action", deleteUrl + taskId);
    }

    var calendarEl = document.getElementById("calendar");
    if (calendarEl) {
        var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: "dayGridMonth",
            locale: "es",
            headerToolbar: {
                left: "prev,next today",
                center: "title",
                right: "dayGridMonth,timeGridWeek,timeGridDay",
            },
            // --- MODIFICACIÓN CLAVE AQUÍ ---
            // FullCalendar usará esta función para obtener los eventos
            events: function(fetchInfo, successCallback, failureCallback) {
                const token = getJwtToken();
                if (!token) {
                    console.error("No se encontró el token JWT. El usuario no está autenticado para llamadas a la API.");
                    failureCallback({ message: "No autenticado" });
                    alert("Su sesión ha expirado o no tiene permisos. Por favor, inicie sesión de nuevo.");
                    window.location.href = '/login'; // Redirigir a login si no hay token
                    return;
                }

                // FullCalendar ya añade start y end a la URL, así que los usamos
                const url = `/api/tasks-calendar?start=${fetchInfo.startStr}&end=${fetchInfo.endStr}`;

                fetch(url, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}` // ¡Añadimos el token aquí!
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        if (response.status === 401 || response.status === 403) {
                            console.error("Error de autenticación o acceso no autorizado al cargar tareas del calendario.");
                            failureCallback({ message: "No autorizado" });
                            alert("Su sesión ha expirado o no tiene permisos. Por favor, inicie sesión de nuevo.");
                            window.location.href = '/login';
                        }
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Tareas cargadas para el calendario:', data);
                    successCallback(data); // Pasa los datos a FullCalendar
                })
                .catch(error => {
                    console.error('Error al cargar las tareas del calendario:', error);
                    failureCallback(error);
                });
            },
            // --- FIN DE MODIFICACIÓN CLAVE ---
            eventClick: function (info) {
                var task = info.event;
                var taskId = task.id;

                $("#modalTaskTitle").text(task.title);
                $("#modalTaskDueDate").text(
                    task.start ? new Date(task.start).toLocaleDateString("es-ES") : "N/A"
                );
                $("#modalTaskPriority").text(task.extendedProps.priority || "-");
                $("#modalTaskStatus").text(task.extendedProps.status || "-");
                $("#modalTaskEtiqueta").text(task.extendedProps.etiqueta || "-");
                $("#modalTaskDescription").text(
                    task.extendedProps.description || "Sin descripción."
                );

                $("#calendarEditButton")
                    .off("click")
                    .on("click", function () {
                        $("#calendarTaskDetailsModal").modal("hide");
                        // Al hacer clic en Editar desde el modal de detalles del calendario,
                        // usamos prepareEditTaskFormModal que ya maneja la lógica de ocultar 'Completada'.
                        prepareEditTaskFormModal(
                            taskId,
                            task.title,
                            task.extendedProps.description,
                            task.start ? new Date(task.start).toISOString().split('T')[0] : "", // Formato YYYY-MM-DD
                            task.extendedProps.priority,
                            task.extendedProps.status,
                            task.extendedProps.etiqueta
                        );
                        $("#editTaskModal").modal("show");
                    });

                $("#calendarDeleteButton")
                    .off("click")
                    .on("click", function () {
                        $("#calendarTaskDetailsModal").modal("hide");
                        $("#taskTitleToDeleteModal").text(task.title);
                        $("#deleteTaskFormInModal").attr("action", "/tasks/delete/" + taskId);
                        $("#deleteTaskModal").modal("show");
                    });

                $("#calendarTaskDetailsModal").modal("show");
            },
            eventChange: function (info) {
                // Si permites arrastrar y soltar eventos, aquí actualizarías la tarea
                // Esto requeriría otra llamada AJAX con el token JWT
            },
        });
        calendar.render();

        $("#editTaskModal").on("hidden.bs.modal", function () {
            calendar.refetchEvents();
        });
        $("#deleteTaskModal").on("hidden.bs.modal", function () {
            calendar.refetchEvents();
        });
    }
});