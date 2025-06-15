// Puedes añadir esto a un controlador existente o crear uno nuevo llamado AdminController.java
package com.freedom.tareas.Controller; // Asegúrate de que el paquete sea correcto

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController { // Puedes llamarlo como quieras, o usar un controlador existente

    @GetMapping("/admin")
    public String showAdminDashboard() {
        // Esto devolverá el nombre de tu plantilla HTML para el panel de administración.
        // Asegúrate de que tienes un archivo src/main/resources/templates/admin/dashboard.html (o el path que uses).
        return "admin"; // Ajusta este path si tu archivo HTML está en otra ubicación
    }

}