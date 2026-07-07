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
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(ErrorSchema.businessError(ex.getMessage(), ex.getMessage())))
                .build();
    }
}
