package com.example.template.service;

import com.example.template.dto.request.EndpointPermissionRequest;
import com.example.template.dto.response.EndpointPermissionResponse;
import com.example.template.entity.EndpointPermission;
import com.example.template.exception.ResourceNotFoundException;
import com.example.template.repository.EndpointPermissionRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk mengelola aturan otorisasi dinamis.
 *
 * CARA KERJA CACHE:
 * - Saat startup (StartupEvent priority 10), semua rule aktif dimuat ke memori.
 * - Setiap perubahan (create/update/delete) otomatis memanggil refresh().
 * - refresh() bisa juga dipanggil manual via endpoint /api/permissions/refresh.
 * - Field permissionCache bersifat volatile → penggantian reference-nya thread-safe.
 */
@ApplicationScoped
@Slf4j
public class EndpointPermissionService {

    @Inject
    EndpointPermissionRepository repository;

    private volatile List<EndpointPermission> permissionCache = Collections.emptyList();

    void loadPermissions(@Observes @Priority(10) StartupEvent ev) {
        refresh();
    }

    public void refresh() {
        List<EndpointPermission> loaded = repository.findAllByActiveTrueOrderBySortOrderAsc();
        permissionCache = loaded;
        log.info("Permission cache refreshed: {} active rules loaded", loaded.size());
    }

    public Optional<EndpointPermission> findMatchingPermission(String method, String path) {
        List<EndpointPermission> perms = permissionCache;

        if (perms.isEmpty()) {
            refresh();
            perms = permissionCache;
        }

        return perms.stream()
                .filter(p -> matchesMethod(p.getHttpMethod(), method))
                .filter(p -> antMatch(p.getUrlPattern(), path))
                .findFirst();
    }

    public List<EndpointPermissionResponse> findAll() {
        return repository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(EndpointPermissionResponse::from)
                .toList();
    }

    public EndpointPermissionResponse findById(Long id) {
        return repository.findByIdOptional(id)
                .map(EndpointPermissionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("EndpointPermission", id));
    }

    @Transactional
    public EndpointPermissionResponse create(EndpointPermissionRequest request) {
        EndpointPermission permission = EndpointPermission.builder()
                .httpMethod(request.getHttpMethod().toUpperCase())
                .urlPattern(request.getUrlPattern())
                .requiredRole(request.getRequiredRole())
                .sortOrder(request.getSortOrder())
                .active(request.isActive())
                .description(request.getDescription())
                .build();

        repository.persist(permission);
        refresh();
        log.info("Permission rule dibuat: [{} {}] -> {}", permission.getHttpMethod(), permission.getUrlPattern(), permission.getRequiredRole());
        return EndpointPermissionResponse.from(permission);
    }

    @Transactional
    public EndpointPermissionResponse update(Long id, EndpointPermissionRequest request) {
        EndpointPermission existing = repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("EndpointPermission", id));

        existing.setHttpMethod(request.getHttpMethod().toUpperCase());
        existing.setUrlPattern(request.getUrlPattern());
        existing.setRequiredRole(request.getRequiredRole());
        existing.setSortOrder(request.getSortOrder());
        existing.setActive(request.isActive());
        existing.setDescription(request.getDescription());

        refresh();
        log.info("Permission rule diupdate: [{} {}] -> {}", existing.getHttpMethod(), existing.getUrlPattern(), existing.getRequiredRole());
        return EndpointPermissionResponse.from(existing);
    }

    @Transactional
    public void delete(Long id) {
        EndpointPermission permission = repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("EndpointPermission", id));
        repository.delete(permission);
        refresh();
        log.info("Permission rule dihapus: [{} {}]", permission.getHttpMethod(), permission.getUrlPattern());
    }

    private boolean matchesMethod(String ruleMethod, String requestMethod) {
        return "*".equals(ruleMethod) || ruleMethod.equalsIgnoreCase(requestMethod);
    }

    /**
     * Ant-style path matching. Mendukung ** (multi-segment) dan * (single-segment).
     * Menggantikan Spring's AntPathMatcher.
     */
    private boolean antMatch(String pattern, String path) {
        // Handle /** first: /api/x/** should match /api/x AND /api/x/anything
        String regex = pattern
                .replace(".", "\\.")
                .replace("/**", "SLASHDOUBLESTAR")
                .replace("**", ".*")
                .replace("*", "[^/]*")
                .replace("SLASHDOUBLESTAR", "(/.*)?");
        return path.matches(regex);
    }
}
