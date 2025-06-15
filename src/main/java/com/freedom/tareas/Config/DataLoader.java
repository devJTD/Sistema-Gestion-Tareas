package com.freedom.tareas.Config; // Puedes ponerlo en Config o en un paquete util.

import com.freedom.tareas.Model.Role;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica si ya existe un usuario ADMIN.
        // Puedes buscar por username fijo o por la existencia de cualquier ADMIN.
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Si no existe, crea el usuario ADMIN
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("adminpass")); // ¡Cambia esta contraseña por una fuerte en producción!
            adminUser.setRole(Role.ADMIN); // Asigna el rol ADMIN

            userRepository.save(adminUser);
            System.out.println("Usuario ADMIN 'admin' creado exitosamente.");
        } else {
            System.out.println("Usuario ADMIN 'admin' ya existe. No se creó uno nuevo.");
        }
    }
}