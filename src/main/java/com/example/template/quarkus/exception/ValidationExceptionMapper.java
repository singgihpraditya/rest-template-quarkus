package com.example.template.quarkus.exception;

import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.ErrorSchema;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Provider
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", detail);

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(ErrorSchema.validationError(
                        "Validation failed: " + detail,
                        "Validasi gagal: " + detail
                )))
                .build();
    }
}
