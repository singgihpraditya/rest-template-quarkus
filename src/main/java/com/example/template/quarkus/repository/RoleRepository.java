package com.example.template.quarkus.repository;

import com.example.template.quarkus.entity.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Optional<Role> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
