package com.example.template.quarkus.controller;

import com.example.template.quarkus.aspect.Logged;
import com.example.template.quarkus.dto.request.EndpointPermissionRequest;
import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.EndpointPermissionResponse;
import com.example.template.quarkus.service.EndpointPermissionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Endpoint Permission", description = "Manajemen aturan otorisasi dinamis (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@Logged
@Slf4j
public class EndpointPermissionController {

    @Inject
    EndpointPermissionService endpointPermissionService;

    @GET
    @Operation(summary = "List Permission", description = "Ambil semua aturan otorisasi yang aktif")
    public Response getAll() {
        List<EndpointPermissionResponse> result = endpointPermissionService.findAll();
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail Permission")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(endpointPermissionService.findById(id))).build();
    }

    @POST
    @Operation(summary = "Tambah Permission")
    public Response create(@Valid EndpointPermissionRequest request) {
        EndpointPermissionResponse created = endpointPermissionService.create(request);
        return Response.status(Response.Status.CREATED).entity(ApiResponse.success(created)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update Permission")
    public Response update(@PathParam("id") Long id, @Valid EndpointPermissionRequest request) {
        return Response.ok(ApiResponse.success(endpointPermissionService.update(id, request))).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Hapus Permission")
    public Response delete(@PathParam("id") Long id) {
        endpointPermissionService.delete(id);
        return Response.ok(ApiResponse.success(null)).build();
    }
}
