package com.freedom.tareas.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.freedom.tareas.Model.Role;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.UserRepository;

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
        System.out.println("LOG: Ejecutando DataLoader para verificar usuario ADMIN.");
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("adminpass"));
            adminUser.setRole(Role.ADMIN);

            userRepository.save(adminUser);
            System.out.println("LOG: Usuario ADMIN 'admin' creado exitosamente.");
        } else {
            System.out.println("LOG: Usuario ADMIN 'admin' ya existe. No se creó uno nuevo.");
        }
    }
}
