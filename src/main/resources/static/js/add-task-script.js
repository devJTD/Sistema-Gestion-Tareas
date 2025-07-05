// Sección de Preparación de Modal de Añadir Tarea
// Restablece y prepara el modal para agregar una nueva tarea.
function prepareAddTaskFormModal() {
    $("#taskFormModalLabel").html(
        '<i class="fas fa-plus-circle"></i> Agregar Nueva Tarea'
    );
    $("#taskForm").attr("action", "/tasks/add");
    $("#taskForm")[0].reset(); // Restablece el formulario a sus valores iniciales.
    $("#taskId").val("");
    $("#taskPriority").val("");
    $("#taskStatus").val("");
    $("#etiqueta").val("");
}

// Sección de Inicialización de Documento
// Ejecuta el código una vez que el DOM está completamente cargado.
$(document).ready(function () {
    // Verifica si el formulario de tareas existe en la página.
    if ($("#taskForm").length) {
        // Maneja el evento de envío del formulario de tareas.
        $("#taskForm").submit(function (event) {
            let isValid = true;
            // Itera sobre todos los campos "required" para validar su contenido.
            $(this)
                .find("[required]")
                .each(function () {
                    if (
                        $(this).val() === "" ||
                        ($(this).is("select") && $(this).val() === null)
                    ) {
                        isValid = false;
                        $(this).addClass("is-invalid"); // Añade clase para indicar error de validación.
                    } else {
                        $(this).removeClass("is-invalid"); // Remueve clase si la validación es correcta.
                    }
                });

            // Si la validación falla, previene el envío del formulario.
            if (!isValid) {
                event.preventDefault();
            }
        });
    }
});
