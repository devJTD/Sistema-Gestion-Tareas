$(document).ready(function () {
    // Configura el tema oscuro/claro y lo guarda en el almacenamiento local
    const currentTheme = localStorage.getItem("theme")
        ? localStorage.getItem("theme")
        : null;
    if (currentTheme) {
        $("body").removeClass("theme-light theme-dark").addClass(currentTheme); // Aplica el tema guardado
        if ($("#theme-toggle").length) {
            $("#theme-toggle").prop("checked", currentTheme === "theme-dark"); // Activa el interruptor según el tema
        }
    } else {
        $("body").addClass("theme-light"); // Si no hay tema guardado, aplica tema claro
        if ($("#theme-toggle").length) {
            $("#theme-toggle").prop("checked", false); // Desmarca el interruptor
        }
    }

    // Cambia el tema cuando se selecciona el interruptor
    $("#theme-toggle").change(function () {
        if ($(this).is(":checked")) {
            $("body").removeClass("theme-light").addClass("theme-dark"); // Cambia a tema oscuro
            localStorage.setItem("theme", "theme-dark"); // Guarda el tema
        } else {
            $("body").removeClass("theme-dark").addClass("theme-light"); // Cambia a tema claro
            localStorage.setItem("theme", "theme-light"); // Guarda el tema
        }
    });
});