$(document).ready(function () {
    const currentTheme = localStorage.getItem("theme")
        ? localStorage.getItem("theme")
        : null;

    if (currentTheme) {
        $("body").removeClass("theme-light theme-dark").addClass(currentTheme);
        if ($("#theme-toggle").length) {
            $("#theme-toggle").prop("checked", currentTheme === "theme-dark");
        }
    } else {
        $("body").addClass("theme-light");
        if ($("#theme-toggle").length) {
            $("#theme-toggle").prop("checked", false);
        }
    }

    $("#theme-toggle").change(function () {
        if ($(this).is(":checked")) {
            $("body").removeClass("theme-light").addClass("theme-dark");
            localStorage.setItem("theme", "theme-dark");
        } else {
            $("body").removeClass("theme-dark").addClass("theme-light");
            localStorage.setItem("theme", "theme-light");
        }
    });
});