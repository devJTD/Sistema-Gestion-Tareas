package com.freedom.tareas.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freedom.tareas.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca un usuario por su nombre de usuario.
    Optional<User> findByUsername(String username);

    // Busca un usuario por su dirección de correo electrónico.
    Optional<User> findByEmail(String email);

    // Verifica si ya existe un usuario con el nombre de usuario dado.
    boolean existsByUsername(String username);

    // Verifica si ya existe un usuario con la dirección de correo electrónico dada.
    boolean existsByEmail(String email);
}