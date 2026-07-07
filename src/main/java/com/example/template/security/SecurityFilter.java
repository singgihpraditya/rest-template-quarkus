package com.example.template.security;

import com.example.template.entity.EndpointPermission;
import com.example.template.service.EndpointPermissionService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * SecurityFilter: kombinasi JWT validation + dynamic authorization dalam satu filter.
 *
 * Menggantikan:
 * - Spring's JwtAuthenticationFilter (validasi token, set SecurityContext)
 * - DynamicAuthorizationManager (cek permission dari DB)
 * - JwtAuthenticationEntryPoint (401 response)
 * - JwtAccessDeniedHandler (403 response)
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    JwtTokenProvider jwtTokenProvider;

    @Inject
    EndpointPermissionService endpointPermissionService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        Optional<EndpointPermission> matchingPermission = endpointPermissionService
                .findMatchingPermission(method, path);

        // Tidak ada rule yang cocok → tolak (default deny)
        if (matchingPermission.isEmpty()) {
            log.warn("Tidak ada rule permission untuk [{} {}]", method, path);
            requestContext.abortWith(buildUnauthorizedResponse("Akses ditolak"));
            return;
        }

        EndpointPermission permission = matchingPermission.get();
        String requiredRole = permission.getRequiredRole();

        // Rule publik: tidak butuh token
        if (requiredRole == null || requiredRole.isBlank()) {
            return;
        }

        // Butuh token: validasi JWT
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(buildUnauthorizedResponse("Token tidak ditemukan atau tidak valid"));
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            requestContext.abortWith(buildUnauthorizedResponse("Token tidak valid atau kadaluarsa"));
            return;
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // Cek role
        if (!roles.contains(requiredRole)) {
            log.warn("User {} tidak memiliki role {} untuk [{} {}]", username, requiredRole, method, path);
            requestContext.abortWith(buildForbiddenResponse("Akses ditolak: tidak memiliki role yang diperlukan"));
            return;
        }

        // Set SecurityContext agar controller bisa mengambil username via @Context SecurityContext
        String finalPath = path;
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String role) {
                return roles.contains(role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }

    private Response buildUnauthorizedResponse(String message) {
        String body = """
                {"error_schema":{"error_code":"RST-401","error_message":{"english":"%s","indonesian":"%s"}}}
                """.formatted(message, message).trim();
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(body)
                .type("application/json")
                .build();
    }

    private Response buildForbiddenResponse(String message) {
        String body = """
                {"error_schema":{"error_code":"RST-403","error_message":{"english":"%s","indonesian":"%s"}}}
                """.formatted(message, message).trim();
        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type("application/json")
                .build();
    }
}
