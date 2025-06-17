$(document).ready(function () {
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
            events: "/api/tasks-calendar",
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