// Prepara el modal para editar una tarea existente
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

// Prepara el modal para eliminar una tarea
function prepareDeleteTaskModal(taskId, taskTitle) {
    $("#taskTitleToDeleteModal").text(taskTitle); // Muestra el título de la tarea en el modal
    var deleteUrl = "/tasks/delete/";
    $("#deleteTaskFormInModal").attr("action", deleteUrl + taskId); // Establece la URL de eliminación
}

// Función para rotar entre consejos (tips) de manera periódica
function initializeTipRotation(tipsArray, elementSelector, intervalTime) {
    $(document).ready(function () {
        var tips = tipsArray; // Lista de consejos
        var tipElement = $(elementSelector); // Elemento donde se muestran los consejos
        var currentTipIndex = 0;

        // Si el elemento existe y tiene consejos
        if (tipElement.length > 0 && tips && tips.length > 0) {
            var initialTipText = tipElement.text().trim();
            currentTipIndex = tips.indexOf(initialTipText); // Encuentra el índice del consejo actual

            // Si es el primer consejo o solo hay uno, comienza desde el inicio
            if (currentTipIndex === -1 || tips.length === 1) {
                currentTipIndex = 0;
            } else {
                currentTipIndex = (currentTipIndex + 1) % tips.length; // Rotar entre los consejos
            }

            // Si hay más de un consejo, rota entre ellos
            if (tips.length > 1) {
                setInterval(function () {
                    var nextTip = tips[currentTipIndex];
                    tipElement.fadeOut(500, function () {
                        tipElement.text(nextTip).fadeIn(500); // Muestra el siguiente consejo
                    });
                    currentTipIndex = (currentTipIndex + 1) % tips.length; // Actualiza el índice
                }, intervalTime);
            } else {
                tipElement.text(initialTipText); // Si solo hay un consejo, lo muestra
            }
        } else if (tipElement.length > 0) {
            tipElement.text("No hay consejos para mostrar."); // Muestra un mensaje si no hay consejos
        }
    });
}

$(document).ready(function () {
    // Configura la acción para eliminar tareas desde el modal
    if ($("#deleteTaskModal").length) {
        $(".delete-task-btn").on("click", function () {
            var taskId = $(this).data("task-id");
            var taskTitle = $(this).data("task-title");
            prepareDeleteTaskModal(taskId, taskTitle); // Prepara el modal de eliminación
        });
    }

    // Configura la acción para editar tareas desde el modal
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
            ); // Prepara el modal de edición
        });
    }

    // Manejador para el modal de edición
    $("#editTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget); // Botón que activó el modal
        // Obtener datos de los atributos data-*
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");
        var taskDescription = button.data("task-description");
        var taskDueDate = button.data("task-duedate");
        var taskPriority = button.data("task-priority");
        var taskStatus = button.data("task-status");
        var taskEtiqueta = button.data("task-etiqueta");

        // Llenar el formulario del modal con los datos de la tarea
        var modal = $(this);
        modal.find("#editTaskId").val(taskId);
        modal.find("#editTaskTitle").val(taskTitle);
        modal.find("#editTaskDescription").val(taskDescription);
        modal.find("#editTaskDueDate").val(taskDueDate); // Formato YYYY-MM-DD
        modal.find("#editTaskPriority").val(taskPriority);
        modal.find("#editTaskStatus").val(taskStatus);
        modal.find("#editTaskEtiqueta").val(taskEtiqueta);
    });

    // Manejador para el modal de eliminación
    $("#deleteTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget); // Botón que activó el modal
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");

        var modal = $(this);
        modal.find("#taskTitleToDeleteModal").text(taskTitle);
        // Construir la acción del formulario con el ID de la tarea
        modal
            .find("#deleteTaskFormInModal")
            .attr("action", "/tasks/delete/" + taskId);
    });

    // Inicialización de la rotación de consejos (si existe en esta página)
    var allTipsFromThymeleaf = /*[[${allTips}]]*/ []; // Esto es para Thymeleaf. Asegúrate de que esta variable esté disponible.
    initializeTipRotation(allTipsFromThymeleaf, "#rotating-tip", 10000);
});