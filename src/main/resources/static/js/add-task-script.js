// Prepara el modal para agregar una nueva tarea
function prepareAddTaskFormModal() {
    $("#taskFormModalLabel").html(
        '<i class="fas fa-plus-circle"></i> Agregar Nueva Tarea'
    );
    $("#taskForm").attr("action", "/tasks/add"); // Define la acción del formulario
    $("#taskForm")[0].reset(); // Limpia los campos del formulario
    $("#taskId").val(""); // Resetea el valor de ID de tarea
    $("#taskPriority").val(""); // Resetea la prioridad
    $("#taskStatus").val(""); // Resetea el estado
    $("#etiqueta").val(""); // Resetea la etiqueta
}

// Validación de formulario antes de enviar (solo para tareas)
$(document).ready(function () {
    // Si existe el formulario de tarea, valida los campos obligatorios
    if ($("#taskForm").length) {
        $("#taskForm").submit(function (event) {
            let isValid = true; // Variable para saber si el formulario es válido
            $(this)
                .find("[required]")
                .each(function () {
                    // Revisa los campos obligatorios
                    if (
                        $(this).val() === "" ||
                        ($(this).is("select") && $(this).val() === null)
                    ) {
                        isValid = false; // Si un campo está vacío, es inválido
                        $(this).addClass("is-invalid"); // Marca el campo como inválido
                    } else {
                        $(this).removeClass("is-invalid"); // Si es válido, remueve la clase
                    }
                });

            if (!isValid) {
                event.preventDefault(); // Si no es válido, no envía el formulario
            }
        });
    }
});