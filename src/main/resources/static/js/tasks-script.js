// Sección de Preparación de Modales de Tareas
// Prepara el modal de edición de tareas con los datos proporcionados.
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

// Prepara el modal de eliminación de tareas con el ID y título de la tarea.
function prepareDeleteTaskModal(taskId, taskTitle) {
    $("#taskTitleToDeleteModal").text(taskTitle);
    var deleteUrl = "/tasks/delete/";
    $("#deleteTaskFormInModal").attr("action", deleteUrl + taskId);
}

// Sección de Rotación de Consejos
// Inicializa la rotación de consejos en un elemento HTML dado un array de consejos y un intervalo.
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

// Sección de Inicialización de Documento
// Ejecuta el código una vez que el DOM está completamente cargado.
$(document).ready(function () {
    // Maneja el clic en los botones de eliminación de tareas para abrir el modal.
    if ($("#deleteTaskModal").length) {
        $(".delete-task-btn").on("click", function () {
            var taskId = $(this).data("task-id");
            var taskTitle = $(this).data("task-title");
            prepareDeleteTaskModal(taskId, taskTitle);
        });
    }

    // Maneja el clic en los botones de edición de tareas para abrir el modal.
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

    // Maneja el evento de mostrar el modal de edición de tareas.
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

    // Maneja el evento de mostrar el modal de eliminación de tareas.
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

    // Obtiene los consejos de Thymeleaf y los usa para inicializar la rotación.
    var allTipsFromThymeleaf = /*[[${allTips}]]*/ [];
    initializeTipRotation(allTipsFromThymeleaf, "#rotating-tip", 10000);
});

// Sección de Actualización de Imagen de Perfil
// Actualiza la imagen de perfil del usuario basándose en una URL proporcionada.
function actualizarImagen() {
    const url = document.getElementById("imagenUrl").value;
    const img = document.getElementById("imagenPerfil");
    if (url) {
        img.src = url;
    } else {
        img.src = "[[@{/images/perfilVacio.jpg}]]";
    }
}
