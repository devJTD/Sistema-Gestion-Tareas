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

    // Inyección de dependencias para repositorios y codificador de contraseñas.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;

    // Constructor que permite a Spring inyectar las dependencias necesarias.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRepository = taskRepository;
    }

    // Crea un nuevo usuario, asigna rol "USER" y codifica la contraseña.
    // Retorna true si se crea, false si el usuario o email ya existen.
    public boolean crearUsuario(User usuario) {
        System.out.println("LOG: Intentando crear nuevo usuario: " + usuario.getUsername());
        if (userRepository.existsByUsername(usuario.getUsername())) {
            System.out.println("LOG: Intento de crear usuario con username existente: " + usuario.getUsername());
            return false;
        }
        if (userRepository.existsByEmail(usuario.getEmail())) {
            System.out.println("LOG: Intento de crear usuario con email existente: " + usuario.getEmail());
            return false;
        }

        usuario.setRole(Role.USER); // Asigna el rol por defecto.
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Codifica la contraseña.

        try {
            userRepository.save(usuario);
            System.out.println(
                    "LOG: Usuario '" + usuario.getUsername() + "' guardado exitosamente con rol: " + usuario.getRole());
            return true;
        } catch (Exception e) {
            System.err
                    .println("LOG ERROR: Error al guardar usuario '" + usuario.getUsername() + "': " + e.getMessage());
            return false;
        }
    }

    // Guarda un usuario existente o nuevo.
    public User guardarUsuario(User user) {
        System.out.println("LOG: Guardando usuario (método genérico): " + user.getUsername());
        User savedUser = userRepository.save(user);
        System.out.println("LOG: Usuario '" + savedUser.getUsername() + "' guardado (método genérico) con ID: "
                + savedUser.getId());
        return savedUser;
    }

    // Elimina un usuario de forma permanente por su ID.
    public void eliminarUsuario(Long id) {
        System.out.println("LOG: Eliminando usuario físicamente por ID: " + id);
        userRepository.deleteById(id);
        System.out.println("LOG: Usuario ID " + id + " eliminado físicamente.");
    }

    // Elimina un usuario y todas sus tareas asociadas dentro de una transacción.
    @Transactional
    public void eliminarUsuarioYSusTareas(Long userId) {
        System.out.println("LOG: Intentando eliminar usuario con ID: " + userId + " y sus tareas.");
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            taskRepository.deleteByUserId(userId); // Elimina todas las tareas del usuario.
            userRepository.deleteById(userId); // Luego elimina al usuario.
            System.out.println("LOG: Usuario ID " + userId + " y sus tareas eliminados exitosamente.");
        } else {
            System.err.println("LOG ERROR: Usuario con ID " + userId + " no encontrado para eliminación.");
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para eliminación.");
        }
    }

    // Carga los detalles del usuario por su nombre de usuario para Spring Security.
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
<<<<<<< HEAD
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())); // Asigna el rol al
                                                                                         // UserDetails.
=======
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())); // Asigna el rol al UserDetails.
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        System.out.println("LOG: UserDetails cargados para '" + username + "' con rol: " + usuario.getRole().name());

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(usuario.getUsername());
        builder.password(usuario.getPassword());
        builder.authorities(authorities);

        return builder.build();
    }

    // Asigna el rol de ADMINISTRADOR a un usuario específico.
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

        user.setRole(Role.ADMIN); // Cambia el rol a ADMIN.
        userRepository.save(user); // Guarda el cambio en la base de datos.
        System.out.println("LOG: Rol ADMIN asignado a usuario con ID " + userId + " exitosamente.");
    }

    // Obtiene el usuario autenticado actualmente en el sistema.
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

<<<<<<< HEAD
    // Actualiza el perfil de un usuario, incluyendo validaciones y cambio de
    // contraseña.
=======
    // Actualiza el perfil de un usuario, incluyendo validaciones y cambio de contraseña.
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
    @Transactional
    public User updateProfile(User userDetails, String newPassword) {
        System.out.println("LOG: Intentando actualizar perfil del usuario con ID: " + userDetails.getId());
        User existingUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    System.err.println("LOG ERROR: Usuario con ID " + userDetails.getId()
                            + " no encontrado para actualizar perfil.");
                    return new UserNotFoundException("Usuario no encontrado con ID: " + userDetails.getId());
                });

        // Validaciones para evitar duplicados de username o email.
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

        // Actualiza los campos permitidos del perfil.
<<<<<<< HEAD
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
=======
        existingUser.setUsername(userDetails.getUsername()); 
        existingUser.setEmail(userDetails.getEmail()); 
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        existingUser.setImageUrl(userDetails.getImageUrl());
        existingUser.setAge(userDetails.getAge());
        existingUser.setBirthDate(userDetails.getBirthDate());
        existingUser.setCountry(userDetails.getCountry());
        System.out.println("LOG: Campos de perfil actualizados en memoria para usuario ID: " + existingUser.getId());

        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword)); // Codifica y actualiza la nueva contraseña.
            System.out.println("LOG: Contraseña actualizada para usuario ID: " + existingUser.getId());
        }

        User updatedUser = userRepository.save(existingUser); // Guarda los cambios en la base de datos.
        System.out.println("LOG: Perfil de usuario ID " + updatedUser.getId() + " guardado en la base de datos.");
        return updatedUser;
    }
    // ---------------------------------------------------------------------

    // Busca un usuario por su ID.
    public Optional<User> buscarUsuarioPorId(Long id) {
        System.out.println("LOG: Buscando usuario por ID: " + id);
        Optional<User> user = userRepository.findById(id);
        System.out.println("LOG: Usuario ID " + id + (user.isPresent() ? " encontrado." : " no encontrado."));
        return user;
    }

    // Busca la entidad de usuario por su nombre de usuario.
    public Optional<User> buscarEntidadPorUsername(String username) {
        System.out.println("LOG: Buscando entidad de usuario por username: " + username);
        Optional<User> user = userRepository.findByUsername(username);
<<<<<<< HEAD
        System.out.println(
                "LOG: Entidad de usuario '" + username + "'" + (user.isPresent() ? " encontrada." : " no encontrada."));
=======
        System.out.println("LOG: Entidad de usuario '" + username + "'" + (user.isPresent() ? " encontrada." : " no encontrada."));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return user;
    }

    // Busca un usuario por su dirección de correo electrónico.
    public Optional<User> buscarPorEmail(String email) {
        System.out.println("LOG: Buscando usuario por email: " + email);
        Optional<User> user = userRepository.findByEmail(email);
<<<<<<< HEAD
        System.out.println(
                "LOG: Usuario con email '" + email + "'" + (user.isPresent() ? " encontrado." : " no encontrado."));
=======
        System.out.println("LOG: Usuario con email '" + email + "'" + (user.isPresent() ? " encontrado." : " no encontrado."));
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
        return user;
    }

    // Recupera una lista de todos los usuarios registrados.
    public List<User> buscarTodosLosUsuarios() {
        System.out.println("LOG: Buscando todos los usuarios.");
        List<User> users = userRepository.findAll();
        System.out.println("LOG: Se encontraron " + users.size() + " usuarios en total.");
        return users;
    }

    // Clase de excepción personalizada para cuando un usuario no es encontrado.
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}