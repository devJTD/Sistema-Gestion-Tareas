package com.freedom.tareas.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freedom.tareas.Model.Role;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.Repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRepository = taskRepository;
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     * Asigna el rol por defecto (USER) y codifica la contraseña.
     * Realiza comprobaciones de existencia de username/email.
     *
     * @param usuario El objeto User a crear.
     * @return true si el usuario fue creado exitosamente, false si ya existe un
     *         usuario con el mismo username o email.
     */
    public boolean crearUsuario(User usuario) {
        System.out.println("LOG: Intentando crear nuevo usuario: " + usuario.getUsername());
        // Comprobaciones de existencia (pueden ser redundantes si el controlador ya las
        // hace,
        // pero es una buena práctica tenerlas también en el servicio para robustez)
        if (userRepository.existsByUsername(usuario.getUsername())) {
            System.out.println("LOG: Intento de crear usuario con username existente: " + usuario.getUsername());
            return false;
        }
        if (userRepository.existsByEmail(usuario.getEmail())) {
            System.out.println("LOG: Intento de crear usuario con email existente: " + usuario.getEmail());
            return false;
        }

        // ¡IMPORTANTE! Asignar el rol aquí antes de guardar
        usuario.setRole(Role.USER);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        try {
            userRepository.save(usuario);
            System.out.println(
                    "LOG: Usuario '" + usuario.getUsername() + "' guardado exitosamente con rol: " + usuario.getRole());
            return true;
        } catch (Exception e) {
            System.err
                    .println("LOG ERROR: Error al guardar usuario '" + usuario.getUsername() + "': " + e.getMessage());
            // Consider logging the stack trace using a logger in production environments
            return false;
        }
    }

    // guardarUsuario
    public User guardarUsuario(User user) {
        System.out.println("LOG: Guardando usuario (método genérico): " + user.getUsername());
        User savedUser = userRepository.save(user);
        System.out.println("LOG: Usuario '" + savedUser.getUsername() + "' guardado (método genérico) con ID: "
                + savedUser.getId());
        return savedUser;
    }

    // eliminarUsuario
    public void eliminarUsuario(Long id) {
        System.out.println("LOG: Eliminando usuario físicamente por ID: " + id);
        userRepository.deleteById(id);
        System.out.println("LOG: Usuario ID " + id + " eliminado físicamente.");
    }

    // eliminarUsuarioYSusTareas
    @Transactional
    public void eliminarUsuarioYSusTareas(Long userId) {
        System.out.println("LOG: Intentando eliminar usuario con ID: " + userId + " y sus tareas.");
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            taskRepository.deleteByUserId(userId);
            System.out.println("LOG: Tareas eliminadas para el usuario ID: " + userId);
            userRepository.deleteById(userId);
            System.out.println("LOG: Usuario ID " + userId + " eliminado exitosamente.");
        } else {
            System.err.println("LOG ERROR: Usuario con ID " + userId + " no encontrado para eliminación.");
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para eliminación.");
        }
    }

    // loadUserByUsername
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("LOG: Intentando cargar UserDetails para el usuario: " + username);
        User usuario = userRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("LOG ERROR: Usuario no encontrado al cargar UserDetails: " + username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
        System.out.println("LOG: UserDetails cargados para '" + username + "' con rol: " + usuario.getRole().name());

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(usuario.getUsername());
        builder.password(usuario.getPassword());
        builder.authorities(authorities);

        return builder.build();
    }

    // asignarRolAdmin
    @Transactional
    public void asignarRolAdmin(Long userId) {
        System.out.println("LOG: Intentando asignar rol ADMIN a usuario con ID: " + userId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            System.err.println(
                    "LOG ERROR: Usuario con ID " + userId + " no encontrado para asignar rol de administrador.");
            throw new UserNotFoundException(
                    "Usuario con ID " + userId + " no encontrado para asignar rol de administrador.");
        }

        User user = userOptional.get();

        if (user.getRole() == Role.ADMIN) {
            System.out.println("LOG: El usuario con ID " + userId + " ya tiene el rol de ADMIN.");
            throw new IllegalArgumentException("El usuario con ID " + userId + " ya tiene el rol de ADMIN.");
        }

        user.setRole(Role.ADMIN);
        userRepository.save(user);
        System.out.println("LOG: Rol ADMIN asignado a usuario con ID " + userId + " exitosamente.");
    }

    // Métodos para obtener y actualizar el usuario logeado
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        System.out.println("LOG: Obteniendo usuario autenticado actual.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("LOG: No hay usuario autenticado o es anónimo.");
            return null;
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println(
                            "LOG ERROR: Usuario autenticado '" + username + "' no encontrado en la base de datos.");
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
        System.out.println("LOG: Usuario autenticado actual obtenido: " + user.getUsername());
        return user;
    }

    // En UserService.java
    @Transactional
    public User updateProfile(User userDetails, String newPassword) {
        System.out.println("LOG: Intentando actualizar perfil del usuario con ID: " + userDetails.getId());
        User existingUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    System.err.println("LOG ERROR: Usuario con ID " + userDetails.getId()
                            + " no encontrado para actualizar perfil.");
                    return new UserNotFoundException("Usuario no encontrado con ID: " + userDetails.getId());
                });

        // Validaciones para username y email (opcional, pero muy recomendable)
        // Antes de actualizar, verifica si el nuevo username o email ya existen para
        // otro usuario.
        // Esto es crucial para evitar duplicados y errores en la base de datos.
        if (!existingUser.getUsername().equals(userDetails.getUsername())
                && userRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario '" + userDetails.getUsername() + "' ya está en uso.");
        }
        if (!existingUser.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException(
                    "El correo electrónico '" + userDetails.getEmail() + "' ya está en uso.");
        }

        // Actualiza los campos que se permiten cambiar
        existingUser.setUsername(userDetails.getUsername()); // ¡¡¡AÑADE ESTA LÍNEA!!!
        existingUser.setEmail(userDetails.getEmail()); // ¡¡¡ASEGÚRATE DE QUE ESTA LÍNEA ESTÉ!!!
        existingUser.setImageUrl(userDetails.getImageUrl());
        existingUser.setAge(userDetails.getAge());
        existingUser.setBirthDate(userDetails.getBirthDate());
        existingUser.setCountry(userDetails.getCountry());
        System.out.println("LOG: Campos de perfil actualizados en memoria para usuario ID: " + existingUser.getId());

        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            System.out.println("LOG: Contraseña actualizada para usuario ID: " + existingUser.getId());
        }

        // Guardar los cambios
        User updatedUser = userRepository.save(existingUser);
        System.out.println("LOG: Perfil de usuario ID " + updatedUser.getId() + " guardado en la base de datos.");
        return updatedUser;
    }
    // ---------------------------------------------------------------------

    // buscarUsuarioPorId
    public Optional<User> buscarUsuarioPorId(Long id) {
        System.out.println("LOG: Buscando usuario por ID: " + id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            System.out.println("LOG: Usuario ID " + id + " encontrado.");
        } else {
            System.out.println("LOG: Usuario ID " + id + " no encontrado.");
        }
        return user;
    }

    // buscarUsuarioPorUsername
    public Optional<User> buscarEntidadPorUsername(String username) {
        System.out.println("LOG: Buscando entidad de usuario por username: " + username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            System.out.println("LOG: Entidad de usuario '" + username + "' encontrada.");
        } else {
            System.out.println("LOG: Entidad de usuario '" + username + "' no encontrada.");
        }
        return user;
    }

    // buscarPorEmail
    public Optional<User> buscarPorEmail(String email) {
        System.out.println("LOG: Buscando usuario por email: " + email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            System.out.println("LOG: Usuario con email '" + email + "' encontrado.");
        } else {
            System.out.println("LOG: Usuario con email '" + email + "' no encontrado.");
        }
        return user;
    }

    // buscarTodosLosUsuarios
    public List<User> buscarTodosLosUsuarios() {
        System.out.println("LOG: Buscando todos los usuarios.");
        List<User> users = userRepository.findAll();
        System.out.println("LOG: Se encontraron " + users.size() + " usuarios en total.");
        return users;
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
