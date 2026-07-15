package com.example.template.quarkus.controller;

import com.example.template.quarkus.aspect.Logged;
import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.PostResponse;
import com.example.template.quarkus.service.ExternalApiService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/external")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "External API", description = "Demo pemanggilan REST API eksternal via MicroProfile REST Client")
@Logged
@Slf4j
public class ExternalApiController {

    @Inject
    ExternalApiService externalApiService;

    @GET
    @Path("/posts")
    @Operation(summary = "Semua Post", description = "Ambil semua post dari JSONPlaceholder")
    public Response getAllPosts() {
        List<PostResponse> posts = externalApiService.getAllPosts();
        return Response.ok(ApiResponse.success(posts)).build();
    }

    @GET
    @Path("/posts/{id}")
    @Operation(summary = "Post by ID")
    public Response getPostById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(externalApiService.getPostById(id))).build();
    }

    @GET
    @Path("/posts/user/{userId}")
    @Operation(summary = "Post by User", description = "Ambil post berdasarkan userId")
    public Response getPostsByUser(@PathParam("userId") Long userId) {
        List<PostResponse> posts = externalApiService.getPostsByUserId(userId);
        return Response.ok(ApiResponse.success(posts)).build();
    }
}
