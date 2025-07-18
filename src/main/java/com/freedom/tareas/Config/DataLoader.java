package com.freedom.tareas.Config;

import java.time.LocalDate;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.freedom.tareas.Model.Role;
import com.freedom.tareas.Model.Task;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.Repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;
    private final Random random = new Random();

    // Listas de valores posibles para las tareas
    private final String[] TITLES = {
        "Revisar pull request", "Implementar nueva funcionalidad", "Corregir bug crítico",
        "Diseñar esquema de base de datos", "Preparar presentación", "Escribir documentación técnica",
        "Configurar entorno de desarrollo", "Optimizar rendimiento", "Realizar pruebas de integración",
        "Investigar nueva tecnología", "Planificar reunión de equipo", "Actualizar dependencias",
        "Refactorizar código legado", "Desplegar a staging", "Análisis de requisitos"
    };

    private final String[] DESCRIPTIONS = {
        "Asegurar que el código cumpla con los estándares.", "Añadir la característica X al módulo Y.",
        "Solucionar el problema reportado en el ticket #123.", "Modelar las tablas para el módulo de usuarios.",
        "Crear diapositivas para la demostración del proyecto.", "Detallar el uso de la API REST.",
        "Instalar y configurar las herramientas necesarias.", "Identificar y eliminar cuellos de botella.",
        "Verificar la interacción entre los diferentes componentes.", "Evaluar la viabilidad de Z framework.",
        "Definir agenda y objetivos para la sesión.", "Actualizar librerías a sus últimas versiones.",
        "Mejorar la legibilidad y mantenibilidad del código.", "Publicar la nueva versión para pruebas internas.",
        "Recopilar y analizar las necesidades del cliente."
    };

    private final String[] PRIORITIES = {"Alta", "Media", "Baja"}; 
    private final String[] STATUSES = {"Pendiente", "En Proceso", "Completada"}; 
    private final String[] ETIQUETAS = {"Trabajo", "Personal", "Urgente", "Desarrollo", "Backend", "Frontend", "DevOps", "QA", "Documentación", "Reunión", "Base de Datos", "Diseño", "Comunicación"};


    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("LOG: Ejecutando DataLoader para verificar usuarios y tareas.");

        // --- Inicialización de Usuarios ---
        User adminUser = initializeUser("admin", "admin@example.com", "adminpass", Role.ADMIN);
        User jeanpierreUser = initializeUser("jeanpierre", "jeanpierre@gmail.com", "jeanpierre123", Role.USER);
        User oscarUser = initializeUser("oscar", "oscar@gmail.com", "oscar123", Role.USER);
        User neisonUser = initializeUser("neison", "neison@gmail.com", "neison123", Role.USER);

        // --- Inicialización de Tareas ---
        createRandomTasksForUser(adminUser, 5);
        createRandomTasksForUser(jeanpierreUser, 5);
        createRandomTasksForUser(oscarUser, 5);
        createRandomTasksForUser(neisonUser, 5);

        System.out.println("LOG: Inicialización de datos completada."); // Mensaje final
    }

    // Método auxiliar para inicializar un usuario
    private User initializeUser(String username, String email, String password, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setRole(role);
            userRepository.save(newUser);
            System.out.println("LOG: Usuario " + role.name() + " '" + username + "' creado exitosamente.");
            return newUser;
        });
    }

    // Método auxiliar para crear y guardar tareas aleatorias para un usuario
    private void createRandomTasksForUser(User user, int numberOfTasks) {
        // Usar findByUser_IdAndActiveOnPage para verificar si el usuario ya tiene tareas activas
        if (user != null && taskRepository.findByUser_IdAndActiveOnPage(user.getId(), "on").isEmpty()) {
            System.out.println("LOG: Inicializando " + numberOfTasks + " tareas aleatorias para el usuario: " + user.getUsername());
            for (int i = 0; i < numberOfTasks; i++) {
                Task task = new Task();
                task.setUser(user);
                task.setTitle(TITLES[random.nextInt(TITLES.length)]);
                task.setDescription(DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)]);

                String status = STATUSES[random.nextInt(STATUSES.length)];
                task.setStatus(status);

                LocalDate dueDate;
                LocalDate completionDate = null;

                if ("Completada".equalsIgnoreCase(status)) {
                    // Si la tarea está completada, la fecha de vencimiento puede ser pasada o presente
                    dueDate = LocalDate.now().minusDays(random.nextInt(10));
                    completionDate = LocalDate.now().minusDays(random.nextInt(5));
                } else {
                    // Si está pendiente o en proceso, la fecha de vencimiento debe ser futura
                    dueDate = LocalDate.now().plusDays(random.nextInt(20) + 1); // Vence entre 1 y 20 días
                }
                task.setDueDate(dueDate);
                task.setCompletionDate(completionDate);


                task.setPriority(PRIORITIES[random.nextInt(PRIORITIES.length)]);
                task.setEtiqueta(ETIQUETAS[random.nextInt(ETIQUETAS.length)]);
                task.setActiveOnPage("on");

                taskRepository.save(task);
                System.out.println("LOG: Tarea aleatoria creada: '" + task.getTitle() + "' para " + user.getUsername());
            }
        } else if (user != null) {
            System.out.println("LOG: El usuario '" + user.getUsername() + "' ya tiene tareas activas. No se inicializarán nuevas.");
        } else {
            System.out.println("LOG: No se pudo encontrar el usuario para inicializar tareas.");
        }
    }
}
