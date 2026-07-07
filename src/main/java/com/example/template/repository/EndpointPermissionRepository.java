package com.example.template.repository;

import com.example.template.entity.EndpointPermission;
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
