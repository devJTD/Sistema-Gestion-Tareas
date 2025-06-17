package com.freedom.tareas.Service;

import com.freedom.tareas.Model.Role; // Importa tu enum Role
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.Repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.GrantedAuthority; // Importa GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importa SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // Necesario para ArrayList
import java.util.List; // Necesario para List
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository; // Inyecta el TaskRepository


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRepository = taskRepository;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * Por defecto, asigna el rol USER.
     * @param usuario El objeto Usuario a guardar.
     * @return true si el usuario fue creado, false si ya existe.
     */
    public boolean crearUsuario(User usuario) {
        if (userRepository.existsByUsername(usuario.getUsername()) || userRepository.existsByEmail(usuario.getEmail())) {
            return false;
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole(Role.USER); // <--- Asigna el rol USER por defecto en la creación común
        userRepository.save(usuario);
        return true;
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario para Spring Security.
     * Asigna las autoridades (roles) basadas en el campo 'role' del Usuario.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usuario = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Asigna las autoridades de Spring Security basándose en el Role del Usuario
        if (usuario.getRole() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else { // Si es Role.USER o cualquier otro valor por defecto
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Construye y devuelve el objeto UserDetails de Spring Security
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(usuario.getUsername());
        builder.password(usuario.getPassword());
        builder.authorities(authorities);


        return builder.build();
    }

    public Optional<User> findByUsernameEntity(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * @param user El objeto User a guardar.
     * @return El usuario guardado.
     */
    public User saveUser(User user) {
        // Aquí podrías añadir lógica de negocio, como encriptar la contraseña
        return userRepository.save(user);
    }

    /**
     * Busca un usuario por su ID.
     * @param id El ID del usuario.
     * @return Un Optional que contiene el usuario si se encuentra, o vacío si no.
     */
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param username El nombre de usuario.
     * @return Un Optional que contiene el usuario si se encuentra, o vacío si no.
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * **NUEVO MÉTODO:**
     * Obtiene una lista de todos los usuarios registrados.
     * @return Una lista de objetos User.
     */
    public List<User> findAllUsers() {
        return userRepository.findAll(); // Llama al método findAll() de JpaRepository
    }

    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar.
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional // Asegura que la eliminación de usuario y tareas sea atómica
    public void deleteUserAndAssociatedTasks(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            // Eliminar todas las tareas asociadas a este usuario primero
            taskRepository.deleteByUserId(userId); // Asume que tienes este método en tu TaskRepository

            // Luego, eliminar el usuario
            userRepository.deleteById(userId);
        } else {
            throw new IllegalArgumentException("Usuario con ID " + userId + " no encontrado.");
        }
    }
}