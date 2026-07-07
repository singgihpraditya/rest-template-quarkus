package com.example.template.exception;

import com.example.template.dto.response.ApiResponse;
import com.example.template.dto.response.ErrorSchema;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(ErrorSchema.unauthorized(ex.getMessage(), ex.getMessage())))
                .build();
    }
}
