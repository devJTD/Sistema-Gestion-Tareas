// Sección de Preparación de Modales de Tareas

/**
 * Prepara el modal de edición de tareas con los datos proporcionados.
 * Se ha ajustado para no mostrar "Completada" en el selector de estado,
 * ya que ahora hay un botón específico para marcar como completada.
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

    // Configurar el select de estado: solo Pendiente y En Proceso
    var $editTaskStatus = $("#editTaskStatus");
    $editTaskStatus.empty(); // Limpiar opciones existentes
    $editTaskStatus.append('<option value="Pendiente">Pendiente</option>');
    $editTaskStatus.append('<option value="En Proceso">En Proceso</option>');

    // Si la tarea está "Completada", se establecerá en "Pendiente" por defecto en el editor,
    // ya que la acción de "Completada" debe hacerse a través del botón específico.
    if (status === 'Completada') {
        $editTaskStatus.val('Pendiente');
    } else {
        $editTaskStatus.val(status);
    }
    
    $("#editTaskEtiqueta").val(etiqueta);
}

// Prepara el modal de eliminación de tareas con el ID y título de la tarea.
function prepareDeleteTaskModal(taskId, taskTitle) {
    $("#taskTitleToDeleteModal").text(taskTitle);
    var deleteUrl = "/tasks/delete/";
    $("#deleteTaskFormInModal").attr("action", deleteUrl + taskId);
}

/**
 * Prepara y muestra el modal de confirmación de tarea completada.
 */
function prepareTaskCompletedModal(taskId, taskTitle) {
    $("#completedTaskTitle").text(taskTitle);
    $("#confirmCompleteForm").attr("action", "/tasks/complete/" + taskId); // Asegura que el formulario de completar tenga la acción correcta
    $("#taskCompletedModal").modal('show');
}


// Sección de Rotación de Consejos
// Función para rotar entre consejos (tips) de manera periódica
function initializeTipRotation(tipsArray, elementSelector, intervalTime) {
    $(document).ready(function () {
        var tips = []; // Inicializamos tips como un array vacío
        var tipElement = $(elementSelector);
        var currentTipIndex = 0;

        // ***** Lógica MODIFICADA para obtener tips de data-attributes *****
        try {
            var dataElement = $("#data-for-js");
            // Obtener el string de consejos del atributo data-all-tips
            var rawTips = dataElement.data("all-tips"); 
            var initialTip = dataElement.data("current-tip");

            // Directamente asume que rawTips es una cadena de texto separada por comas.
            if (typeof rawTips === 'string' && rawTips.length > 0) {
                // Split by comma and trim whitespace from each tip
                tips = rawTips.split(',').map(s => s.trim());
            } else {
                tips = []; // Si rawTips no es una cadena válida o está vacía, el array de tips estará vacío
            }
            
            // Establecer el consejo inicial si está disponible y el elemento existe
            if (tipElement.length > 0 && initialTip) {
                tipElement.text(initialTip);
            }

        } catch (e) {
            console.error("Error al obtener o procesar los consejos del atributo data-:", e);
            tips = []; // Asegura que tips sea un array vacío si hay un error crítico
        }
        // ***** FIN Lógica MODIFICADA *****

        // --- DEBUGGING: Revisa la consola para ver qué consejos se están cargando ---
        console.log("DEBUG: Consejos obtenidos (final) en JS:", tips);
        console.log("DEBUG: Número de consejos:", tips.length);
        console.log("DEBUG: Elemento del tip encontrado:", tipElement.length > 0);
        console.log("DEBUG: Contenido inicial de rotating-tip:", tipElement.text().trim());
        // --- FIN DEBUGGING ---

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
                tipElement.text(tips[0]); // Si solo hay un consejo, muestra el primero
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
    // Esto es un fallback/duplicado de la lógica de prepareEditTaskFormModal.
    // Podrías considerar eliminar uno si la lógica de `prepareEditTaskFormModal` ya es suficiente en el click.
    $("#editTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget);
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");
        var taskDescription = button.data("task-description");
        var taskDueDate = button.data("task-duedate");
        var taskPriority = button.data("task-priority");
        var taskStatus = button.data("task-status");
        var taskEtiqueta = button.data("task-etiqueta");

        // Reutiliza la función de preparación para mantener la lógica consistente
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

    // Maneja el evento de mostrar el modal de eliminación de tareas.
    $("#deleteTaskModal").on("show.bs.modal", function (event) {
        var button = $(event.relatedTarget);
        var taskId = button.data("task-id");
        var taskTitle = button.data("task-title");

        prepareDeleteTaskModal(taskId, taskTitle);
    });

    // NUEVA LÓGICA: Manejo del clic en el botón "Marcar como Completada"
    $(".complete-task-btn").on("click", function () {
        var taskId = $(this).data("task-id");
        var taskTitle = $(this).data("task-title");
        prepareTaskCompletedModal(taskId, taskTitle);
    });

    // Lógica para la descripción "Ver más/Ver menos" (si aplica en tu tasks-style.css)
    $('.description-toggle').on('click', function () {
        var $this = $(this);
        var $toggleText = $this.find('.toggle-text');
        // Alternar el texto "Ver más" y "Ver menos"
        if ($this.attr('aria-expanded') === 'true') {
            $toggleText.text('Ver más');
        } else {
            $toggleText.text('Ver menos');
        }
    });

    // Llamada a la inicialización de la rotación de consejos.
    // Asegúrate de que el elemento `#rotating-tip` exista en tu HTML si lo usas.
    initializeTipRotation([], "#rotating-tip", 10000);
});

// Sección de Actualización de Imagen de Perfil
// Actualiza la imagen de perfil del usuario basándose en una URL proporcionada.
function actualizarImagen() {
    const url = document.getElementById("imagenUrl").value;
    const img = document.getElementById("imagenPerfil");
    if (url) {
        img.src = url;
    } else {
        // Asegúrate de que esta línea ThymeLeaf se procesa ANTES de que el JS se ejecute en el navegador.
        // Si este archivo es un .js puro y no es procesado por Thymeleaf, necesitarás una URL relativa.
        img.src = "/images/perfilVacio.jpg"; 
    }
}