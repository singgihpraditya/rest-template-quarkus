package com.example.template.quarkus.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private ErrorSchema errorSchema;
    private T outputSchema;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .errorSchema(ErrorSchema.success())
                .outputSchema(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorSchema errorSchema) {
        return ApiResponse.<T>builder()
                .errorSchema(errorSchema)
                .build();
    }
}
