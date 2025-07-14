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

function prepareTaskCompletedModal(taskId, taskTitle) {
    $("#completedTaskTitle").text(taskTitle);
    $("#confirmCompleteForm").attr("action", "/tasks/complete/" + taskId);
    $("#taskCompletedModal").modal('show');
}

function initializeTipRotation(tipsArray, elementSelector, intervalTime) {
    $(document).ready(function () {
        var tips = [];
        var tipElement = $(elementSelector);
        var currentTipIndex = 0;

        try {
            var dataElement = $("#data-for-js");
            var rawTips = dataElement.data("all-tips"); 
            var initialTip = dataElement.data("current-tip");

            if (typeof rawTips === 'string' && rawTips.length > 0) {
                tips = rawTips.split(',').map(s => s.trim());
            } else {
                tips = [];
            }
            
            if (tipElement.length > 0 && initialTip) {
                tipElement.text(initialTip);
            }

        } catch (e) {
            console.error("Error al obtener o procesar los consejos del atributo data-:", e);
            tips = [];
        }

        console.log("DEBUG: Consejos obtenidos (final) en JS:", tips);
        console.log("DEBUG: Número de consejos:", tips.length);
        console.log("DEBUG: Elemento del tip encontrado:", tipElement.length > 0);
        console.log("DEBUG: Contenido inicial de rotating-tip:", tipElement.text().trim());

        if (tipElement.length > 0 && tips && tips.length > 0) {
            var initialTipText = tipElement.text().trim();
            currentTipIndex = tips.indexOf(initialTipText);

            if (currentTipIndex === -1 || tips.length === 1) {
                currentTipIndex = 0;
            } else {
                currentTipIndex = (currentTipIndex + 1) % tips.length;
            }

            if (tips.length > 1) {
                setInterval(function () {
                    var nextTip = tips[currentTipIndex];
                    tipElement.fadeOut(500, function () {
                        tipElement.text(nextTip).fadeIn(500);
                    });
                    currentTipIndex = (currentTipIndex + 1) % tips.length;
                }, intervalTime);
            } else {
                tipElement.text(tips[0]);
            }
        } else if (tipElement.length > 0) {
            tipElement.text("No hay consejos para mostrar.");
        }
    });
}

$(document).ready(function () {
    if ($("#deleteTaskModal").length) {
        $(".delete-task-btn").on("click", function () {
            var taskId = $(this).data("task-id");
            var taskTitle = $(this).data("task-title");
            prepareDeleteTaskModal(taskId, taskTitle);
        });
    }

    if ($("#editTaskModal").length) {
        $(".edit-task-btn").on("click", function () {
            var taskId = $(this).data("task-id");
            var taskTitle = $(this).data("task-title");
            var taskDescription = $(this).data("task-description");
            var taskDueDate = $(this).data("task-duedate");
            var taskPriority = $(this).data("task-priority");
            var taskStatus = $(this).data("task-status");
            var taskEtiqueta = $(this).data("task-etiqueta");

            prepareEditTaskFormModal(
                taskId,
                taskTitle,
                taskDescription,
                taskDueDate,
                taskPriority,
                taskStatus,
                taskEtiqueta
            );
        });
    }

    $("#editTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget);
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");
        var taskDescription = button.data("task-description");
        var taskDueDate = button.data("task-duedate");
        var taskPriority = button.data("task-priority");
        var taskStatus = button.data("task-status");
        var taskEtiqueta = button.data("task-etiqueta");

        prepareEditTaskFormModal(
            taskId,
            taskTitle,
            taskDescription,
            taskDueDate,
            taskPriority,
            taskStatus,
            taskEtiqueta
        );
    });

    $("#deleteTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget);
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");

        prepareDeleteTaskModal(taskId, taskTitle);
    });

    $(".complete-task-btn").on("click", function () {
        var taskId = $(this).data("task-id");
        var taskTitle = $(this).data("task-title");
        prepareTaskCompletedModal(taskId, taskTitle);
    });

    $('.description-toggle').on('click', function () {
        var $this = $(this);
        var $toggleText = $this.find('.toggle-text');
        if ($this.attr('aria-expanded') === 'true') {
            $toggleText.text('Ver más');
        } else {
            $toggleText.text('Ver menos');
        }
    });

    initializeTipRotation([], "#rotating-tip", 10000);
});

function actualizarImagen() {
    const url = document.getElementById("imagenUrl").value;
    const img = document.getElementById("imagenPerfil");
    if (url) {
        img.src = url;
    } else {
        img.src = "/images/perfilVacio.jpg"; 
    }
}