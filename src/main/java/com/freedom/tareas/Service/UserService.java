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
        // NOTA: Spring Security prefiere los roles con prefijo "ROLE_", ej., "ROLE_ADMIN"
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
        // Aquí podrías añadir lógica de negocio, como encriptar la contraseña si no se hizo antes
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

    /**
     * Elimina un usuario y todas sus tareas asociadas.
     * @param userId El ID del usuario a eliminar.
     * @throws UserNotFoundException Si el usuario con el ID especificado no se encuentra.
     */
    @Transactional // Asegura que la eliminación de usuario y tareas sea atómica
    public void deleteUserAndAssociatedTasks(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            // Eliminar todas las tareas asociadas a este usuario primero
            // Asegúrate de que TaskRepository.deleteByUserId() exista y funcione correctamente
            taskRepository.deleteByUserId(userId); 

            // Luego, eliminar el usuario
            userRepository.deleteById(userId);
        } else {
            // Lanza la excepción personalizada para un manejo más claro
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para eliminación.");
        }
    }

    /**
     * Asigna el rol de ADMINISTRADOR a un usuario específico.
     * @param userId El ID del usuario al que se le asignará el rol de ADMIN.
     * @throws UserNotFoundException Si el usuario con el ID especificado no se encuentra.
     * @throws IllegalArgumentException Si el usuario ya tiene el rol de ADMIN.
     */
    @Transactional // Asegura que la operación sea atómica: o se completa o se revierte.
    public void assignAdminRole(Long userId) {
        // 1. Buscar el usuario por ID
        Optional<User> userOptional = userRepository.findById(userId);

        // 2. Verificar si el usuario existe
        if (userOptional.isEmpty()) {
            // Lanza una excepción si el usuario no se encuentra.
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para asignar rol de administrador.");
        }

        User user = userOptional.get();

        // 3. Opcional: Verificar si el usuario ya es ADMIN para evitar operaciones innecesarias
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("El usuario con ID " + userId + " ya tiene el rol de ADMIN.");
        }

        // 4. Asignar el nuevo rol a ADMIN
        user.setRole(Role.ADMIN); // Asigna el valor del enum Role.ADMIN

        // 5. Guardar los cambios en la base de datos
        userRepository.save(user);
    }

    /**
     * Excepción personalizada para cuando un usuario no es encontrado.
     * Facilita el manejo específico de este error en los controladores.
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}