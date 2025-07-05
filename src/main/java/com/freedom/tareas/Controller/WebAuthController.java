package com.freedom.tareas.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.freedom.tareas.Model.User;
import com.freedom.tareas.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
class WebAuthController {

    // Sección de Dependencias
    // Inyecta el servicio de usuario para operaciones de autenticación y registro.
    private final UserService userService;

    // Constructor de la clase
    // Inicializa el servicio de usuario.
    public WebAuthController(UserService userService) {
        this.userService = userService;
    }

    // Sección de Inicio de Sesión
    // Muestra la página de inicio de sesión y redirige si el usuario ya está autenticado.
    @GetMapping("/login")
    public String mostrarPaginaLogin(Model model, HttpServletRequest request) {
        System.out.println("LOG: Accediendo a la página de inicio de sesión.");
        model.addAttribute("currentUri", request.getRequestURI());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            System.out.println("LOG: Usuario ya autenticado, redirigiendo a la página principal.");
            return "redirect:/";
        }
        return "login";
    }

    // Sección de Registro (Mostrar Formulario)
    // Muestra el formulario para que un nuevo usuario se registre.
    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        System.out.println("LOG: Accediendo al formulario de registro.");
        model.addAttribute("usuario", new User());
        return "register";
    }

    // Sección de Registro (Procesar Formulario)
    // Procesa el envío del formulario de registro, validando datos y creando el usuario.
    @PostMapping("/register")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") User usuario, BindingResult bindingResult, Model model) {
        System.out.println("LOG: Solicitud de registro para el usuario: " + usuario.getUsername());
        // Maneja errores de validación del formulario.
        if (bindingResult.hasErrors()) {
            System.err.println("LOG ERROR: Errores de validación en el formulario de registro para el usuario: " + usuario.getUsername());
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("- " + error.getDefaultMessage()); // Considerar usar un logger en producción
            }
            return "register";
        }
        // Verifica si el nombre de usuario ya existe.
        if (userService.buscarEntidadPorUsername(usuario.getUsername()).isPresent()) {
            System.err.println("LOG ERROR: Intento de registro con nombre de usuario existente: " + usuario.getUsername());
            model.addAttribute("error", "El nombre de usuario ya está registrado.");
            return "register";
        }
        // Verifica si el correo electrónico ya existe.
        if (userService.buscarPorEmail(usuario.getEmail()).isPresent()) {
            System.err.println("LOG ERROR: Intento de registro con correo electrónico existente: " + usuario.getEmail());
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            return "register";
        }
        // Intenta crear el nuevo usuario.
        boolean creado = userService.crearUsuario(usuario);
        if (!creado) {
            System.err.println("LOG ERROR: No se pudo crear el usuario por un motivo desconocido: " + usuario.getUsername());
            model.addAttribute("error", "No se pudo crear el usuario por un motivo desconocido.");
            return "register";
        }
        System.out.println("LOG: Usuario '" + usuario.getUsername() + "' registrado exitosamente.");
        return "redirect:/login?success";
    }
}
