function prepareAddTaskFormModal() {
    $("#taskFormModalLabel").html(
        '<i class="fas fa-plus-circle"></i> Agregar Nueva Tarea'
    );
    $("#taskForm").attr("action", "/tasks/add");
    $("#taskForm")[0].reset();
    $("#taskId").val("");
    $("#taskPriority").val("");
    $("#taskStatus").val("");
    $("#etiqueta").val("");
}

$(document).ready(function () {
    if ($("#taskForm").length) {
        $("#taskForm").submit(function (event) {
            let isValid = true;
            $(this)
                .find("[required]")
                .each(function () {
                    if (
                        $(this).val() === "" ||
                        ($(this).is("select") && $(this).val() === null)
                    ) {
                        isValid = false;
                        $(this).addClass("is-invalid");
                    } else {
                        $(this).removeClass("is-invalid");
                    }
                });

            if (!isValid) {
                event.preventDefault();
            }
        });
    }
});