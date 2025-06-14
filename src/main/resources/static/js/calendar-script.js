$(document).ready(function () {
    // Estas funciones son globales o se llamarían desde otros scripts, pero para el contexto del calendario se mantienen aquí.
    // Prepara el modal para editar una tarea existente (usada por el calendario)
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
        $("#editTaskId").val(id); // Asigna el ID de la tarea
        $("#editTaskTitle").val(title); // Asigna el título
        $("#editTaskDescription").val(description); // Asigna la descripción
        $("#editTaskDueDate").val(dueDate); // Asigna la fecha de vencimiento
        $("#editTaskPriority").val(priority); // Asigna la prioridad
        $("#editTaskStatus").val(status); // Asigna el estado
        $("#editTaskEtiqueta").val(etiqueta); // Asigna la etiqueta
    }

    // Prepara el modal para eliminar una tarea (usada por el calendario)
    function prepareDeleteTaskModal(taskId, taskTitle) {
        $("#taskTitleToDeleteModal").text(taskTitle); // Muestra el título de la tarea en el modal
        var deleteUrl = "/tasks/delete/";
        $("#deleteTaskFormInModal").attr("action", deleteUrl + taskId); // Establece la URL de eliminación
    }

    // Inicialización del calendario
    var calendarEl = document.getElementById("calendar");
    if (calendarEl) { // Asegura que el elemento del calendario exista
        var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: "dayGridMonth",
            locale: "es",
            headerToolbar: {
                left: "prev,next today",
                center: "title",
                right: "dayGridMonth,timeGridWeek,timeGridDay",
            },
            events: "/api/tasks-calendar", // Asegúrate de que esto sea una URL estática o se procese correctamente por Thymeleaf si es dinámico.
            eventClick: function (info) {
                var task = info.event;
                var taskId = task.id; // Obtener el ID de la tarea

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

                // Cargar datos en el modal de edición y abrirlo
                $("#calendarEditButton")
                    .off("click")
                    .on("click", function () {
                        $("#calendarTaskDetailsModal").modal("hide"); // Ocultar modal de detalles
                        $("#editTaskId").val(taskId);
                        $("#editTaskTitle").val(task.title);
                        $("#editTaskDescription").val(task.extendedProps.description);
                        // Formatear la fecha para el input type="date"
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

                // Cargar datos en el modal de eliminación y abrirlo
                $("#calendarDeleteButton")
                    .off("click")
                    .on("click", function () {
                        $("#calendarTaskDetailsModal").modal("hide"); // Ocultar modal de detalles
                        $("#taskTitleToDeleteModal").text(task.title);
                        // Asegúrate de que el formulario de eliminación tenga la acción correcta con el ID de la tarea
                        $("#deleteTaskFormInModal").attr("action", "/tasks/delete/" + taskId);
                        $("#deleteTaskModal").modal("show");
                    });

                $("#calendarTaskDetailsModal").modal("show");
            },
            // Refrescar calendario después de un cambio (opcional, si usas AJAX para actualizar)
            eventChange: function (info) {
                // Si se arrastran o redimensionan eventos, se podría enviar un AJAX update aquí
                // Por ahora, solo se refrescará con F5 o después de una acción de formulario
            },
        });
        calendar.render();

        // Event listener para recargar el calendario después de cerrar los modales de edición/eliminación
        $("#editTaskModal").on("hidden.bs.modal", function () {
            calendar.refetchEvents(); // Recarga los eventos del calendario
        });
        $("#deleteTaskModal").on("hidden.bs.modal", function () {
            calendar.refetchEvents(); // Recarga los eventos del calendario
        });
    }
});