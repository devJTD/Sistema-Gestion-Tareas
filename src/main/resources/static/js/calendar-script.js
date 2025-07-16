$(document).ready(function () {
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

        var $editTaskStatus = $("#editTaskStatus");
        $editTaskStatus.empty();
        $editTaskStatus.append('<option value="Pendiente">Pendiente</option>');
        $editTaskStatus.append('<option value="En Proceso">En Proceso</option>');
        if (status === 'Completada') {
            $editTaskStatus.val('Pendiente');
        } else {
            $editTaskStatus.val(status);
        }
        
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
            events: function(fetchInfo, successCallback, failureCallback) {
                const token = getJwtToken();
                if (!token) {
                    console.error("No se encontró el token JWT. El usuario no está autenticado para llamadas a la API.");
                    failureCallback({ message: "No autenticado" });
                    alert("Su sesión ha expirado o no tiene permisos. Por favor, inicie sesión de nuevo.");
                    window.location.href = '/login';
                    return;
                }

                const url = `/api/tasks-calendar?start=${fetchInfo.startStr}&end=${fetchInfo.endStr}`;

                fetch(url, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
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
                    successCallback(data);
                })
                .catch(error => {
                    console.error('Error al cargar las tareas del calendario:', error);
                    failureCallback(error);
                });
            },
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
                        prepareEditTaskFormModal(
                            taskId,
                            task.title,
                            task.extendedProps.description,
                            task.start ? new Date(task.start).toISOString().split('T')[0] : "",
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