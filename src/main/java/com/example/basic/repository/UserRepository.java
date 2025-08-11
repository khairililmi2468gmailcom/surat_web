package com.example.basic.repository;

import com.example.basic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Mengganti findByUsername menjadi findByEmail
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

}
