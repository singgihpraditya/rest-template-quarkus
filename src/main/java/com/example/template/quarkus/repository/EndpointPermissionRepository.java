package com.example.template.quarkus.repository;

import com.example.template.quarkus.entity.EndpointPermission;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EndpointPermissionRepository implements PanacheRepository<EndpointPermission> {

    public List<EndpointPermission> findAllByActiveTrueOrderBySortOrderAsc() {
        return find("active = true", Sort.by("sortOrder").ascending()).list();
    }
}
