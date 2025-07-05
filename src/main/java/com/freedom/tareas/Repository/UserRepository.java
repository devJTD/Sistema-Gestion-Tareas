package com.freedom.tareas.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freedom.tareas.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Asegúrate de que este método existe
    boolean existsByUsername(String username);
    boolean existsByEmail(String email); // Asegúrate de que este método existe
}