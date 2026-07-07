package com.example.template.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "endpoint_permissions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EndpointPermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "url_pattern", nullable = false, length = 200)
    private String urlPattern;

    @Column(name = "required_role", length = 50)
    private String requiredRole;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    @Column(name = "description", length = 200)
    private String description;
}
