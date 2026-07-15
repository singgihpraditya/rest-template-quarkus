package com.example.template.quarkus.repository;

import com.example.template.quarkus.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
