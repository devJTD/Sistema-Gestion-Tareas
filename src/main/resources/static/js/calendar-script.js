$(document).ready(function () {
    // Función para obtener el token JWT desde localStorage
    function getJwtToken() {
        return localStorage.getItem('jwtToken');
    }

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
        $("#editTaskStatus").val(status);
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
                        $("#editTaskId").val(taskId);
                        $("#editTaskTitle").val(task.title);
                        $("#editTaskDescription").val(task.extendedProps.description);
                        var dueDate = task.start ? new Date(task.start) : null;
                        if (dueDate) {
                            var year = dueDate.getFullYear();
                            var month = (dueDate.getMonth() + 1).toString().padStart(2, "0");
                            var day = dueDate.getDate().toString().padStart(2, "0");
                            $("#editTaskDueDate").val(`${year}-${month}-${day}`);
                        } else {
                            $("#editTaskDueDate").val("");
                        }
                        $("#editTaskPriority").val(task.extendedProps.priority);
                        $("#editTaskStatus").val(task.extendedProps.status);
                        $("#editTaskEtiqueta").val(task.extendedProps.etiqueta);
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
