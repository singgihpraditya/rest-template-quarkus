package com.example.template.quarkus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EndpointPermissionRequest {

    @NotBlank(message = "HTTP method tidak boleh kosong")
    private String httpMethod;

    @NotBlank(message = "URL pattern tidak boleh kosong")
    private String urlPattern;

    private String requiredRole;

    @NotNull(message = "Sort order tidak boleh kosong")
    private Integer sortOrder;

    private boolean active = true;

    private String description;
}
