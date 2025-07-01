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

import com.freedom.tareas.Model.Role;
import com.freedom.tareas.Model.User;
import com.freedom.tareas.Repository.TaskRepository;
import com.freedom.tareas.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;



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

    // crearUsuario
    public boolean crearUsuario(User usuario) {
        if (userRepository.existsByUsername(usuario.getUsername()) || userRepository.existsByEmail(usuario.getEmail())) {
            return false;
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole(Role.USER);
        userRepository.save(usuario);
        return true;
    }

    // guardarUsuario
    public User guardarUsuario(User user) {
        return userRepository.save(user);
    }

    // eliminarUsuario
    public void eliminarUsuario(Long id) {
        userRepository.deleteById(id);
    }

    // eliminarUsuarioYSusTareas
    @Transactional
    public void eliminarUsuarioYSusTareas(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            taskRepository.deleteByUserId(userId);
            userRepository.deleteById(userId);
        } else {
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para eliminación.");
        }
    }

    // loadUserByUsername
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usuario = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (usuario.getRole() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(usuario.getUsername());
        builder.password(usuario.getPassword());
        builder.authorities(authorities);

        return builder.build();
    }

    // asignarRolAdmin
    @Transactional
    public void asignarRolAdmin(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("Usuario con ID " + userId + " no encontrado para asignar rol de administrador.");
        }

        User user = userOptional.get();

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("El usuario con ID " + userId + " ya tiene el rol de ADMIN.");
        }

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

     // Métodos nuevos para obtener y actualizar el usuario logeado
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; 
        }
        String username = authentication.getName();  
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Transactional
    public User updateProfile(User userDetails, String newPassword) {
        User existingUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userDetails.getId()));

        // Actualiza solo los campos que el usuario puede editar
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setImageUrl(userDetails.getImageUrl());
        existingUser.setAge(userDetails.getAge());
        existingUser.setBirthDate(userDetails.getBirthDate());
        existingUser.setCountry(userDetails.getCountry());

        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(existingUser);
    }
    // ---------------------------------------------------------------------

    // buscarUsuarioPorId
    public Optional<User> buscarUsuarioPorId(Long id) {
        return userRepository.findById(id);
    }

    // buscarUsuarioPorUsername
    public Optional<User> buscarUsuarioPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // buscarEntidadPorUsername
    public Optional<User> buscarEntidadPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // buscarTodosLosUsuarios
    public List<User> buscarTodosLosUsuarios() {
        return userRepository.findAll();
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
