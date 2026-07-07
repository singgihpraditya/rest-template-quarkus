package com.example.template.controller;

import com.example.template.aspect.Logged;
import com.example.template.dto.response.ApiResponse;
import io.quarkus.cache.CacheManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/cache")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cache", description = "Manajemen cache aplikasi (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@Logged
@Slf4j
public class CacheController {

    @Inject
    CacheManager cacheManager;

    @POST
    @Path("/cleanup")
    @Operation(summary = "Bersihkan Semua Cache", description = "Invalidasi semua cache yang terdaftar")
    public Response cleanup() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).ifPresent(cache -> {
                cache.invalidateAll().await().indefinitely();
                log.info("Cache '{}' dibersihkan", cacheName);
            });
        });

        log.info("Semua cache berhasil dibersihkan");
        return Response.ok(ApiResponse.success("Semua cache berhasil dibersihkan")).build();
    }
}
