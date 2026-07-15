package com.example.template.quarkus.dto.response;

import com.example.template.quarkus.entity.EndpointPermission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EndpointPermissionResponse {

    private Long id;
    private String httpMethod;
    private String urlPattern;
    private String requiredRole;
    private int sortOrder;
    private boolean active;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EndpointPermissionResponse from(EndpointPermission permission) {
        return EndpointPermissionResponse.builder()
                .id(permission.getId())
                .httpMethod(permission.getHttpMethod())
                .urlPattern(permission.getUrlPattern())
                .requiredRole(permission.getRequiredRole())
                .sortOrder(permission.getSortOrder())
                .active(permission.isActive())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
