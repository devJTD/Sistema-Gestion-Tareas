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

function initializeTipRotation(tipsArray, elementSelector, intervalTime) {
    $(document).ready(function () {
        var tips = tipsArray;
        var tipElement = $(elementSelector);
        var currentTipIndex = 0;

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
                tipElement.text(initialTipText);
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

        var modal = $(this);
        modal.find("#editTaskId").val(taskId);
        modal.find("#editTaskTitle").val(taskTitle);
        modal.find("#editTaskDescription").val(taskDescription);
        modal.find("#editTaskDueDate").val(taskDueDate);
        modal.find("#editTaskPriority").val(taskPriority);
        modal.find("#editTaskStatus").val(taskStatus);
        modal.find("#editTaskEtiqueta").val(taskEtiqueta);
    });

    $("#deleteTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget);
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");

        var modal = $(this);
        modal.find("#taskTitleToDeleteModal").text(taskTitle);
        modal
            .find("#deleteTaskFormInModal")
            .attr("action", "/tasks/delete/" + taskId);
    });

    var allTipsFromThymeleaf = /*[[${allTips}]]*/ [];
    initializeTipRotation(allTipsFromThymeleaf, "#rotating-tip", 10000);
});