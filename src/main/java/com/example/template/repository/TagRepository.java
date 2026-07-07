package com.example.template.repository;

import com.example.template.entity.Tag;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class TagRepository implements PanacheRepository<Tag> {

    public Optional<Tag> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
