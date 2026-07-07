package com.example.template.controller;

import com.example.template.aspect.Logged;
import com.example.template.dto.request.LoginRequest;
import com.example.template.dto.request.RegisterRequest;
import com.example.template.dto.response.ApiResponse;
import com.example.template.dto.response.TokenResponse;
import com.example.template.dto.response.UserResponse;
import com.example.template.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Autentikasi dan manajemen user")
@Logged
@Slf4j
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Operation(summary = "Login", description = "Login dengan username dan password, dapatkan JWT token")
    public Response login(@Valid LoginRequest request) {
        TokenResponse token = authService.login(request);
        return Response.ok(ApiResponse.success(token)).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "Register", description = "Daftarkan akun baru")
    public Response register(@Valid RegisterRequest request) {
        UserResponse user = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(ApiResponse.success(user)).build();
    }

    @GET
    @Path("/me")
    @Operation(summary = "Current User", description = "Ambil data user yang sedang login")
    @SecurityRequirement(name = "bearerAuth")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        UserResponse user = authService.getCurrentUser(username);
        return Response.ok(ApiResponse.success(user)).build();
    }
}
