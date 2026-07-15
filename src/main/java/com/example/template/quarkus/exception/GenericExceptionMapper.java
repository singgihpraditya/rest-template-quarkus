package com.example.template.quarkus.exception;

import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.ErrorSchema;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Catch-all mapper untuk exception yang tidak ditangani mapper lain.
 * Juga menangani WebApplicationException (NotAuthorizedException, ForbiddenException, dll.)
 * dan mengubahnya ke format ApiResponse standar.
 */
@Provider
@Slf4j
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof NotAuthorizedException) {
            log.warn("Not authorized: {}", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ErrorSchema.unauthorized(
                            "Authentication required", "Autentikasi diperlukan")))
                    .build();
        }

        if (ex instanceof ForbiddenException) {
            log.warn("Forbidden: {}", ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ErrorSchema.forbidden(
                            "Access denied", "Akses ditolak")))
                    .build();
        }

        if (ex instanceof WebApplicationException wae) {
            return Response.status(wae.getResponse().getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ErrorSchema.internalError(
                            wae.getMessage(), wae.getMessage())))
                    .build();
        }

        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(ErrorSchema.internalError(
                        "An unexpected error occurred", "Terjadi kesalahan yang tidak terduga")))
                .build();
    }
}
