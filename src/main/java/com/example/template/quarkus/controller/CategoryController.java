package com.example.template.quarkus.controller;

import com.example.template.quarkus.aspect.Logged;
import com.example.template.quarkus.dto.request.CategoryRequest;
import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.CategoryResponse;
import com.example.template.quarkus.dto.response.PageResponse;
import com.example.template.quarkus.service.CategoryService;
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
import java.util.Map;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Category", description = "Manajemen kategori produk")
@Logged
@Slf4j
public class CategoryController {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(summary = "List Kategori", description = "Ambil semua kategori dengan pagination")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("search") String search) {
        PageResponse<CategoryResponse> result = categoryService.findAll(page, size, search);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail Kategori")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(categoryService.findById(id))).build();
    }

    @POST
    @Operation(summary = "Tambah Kategori")
    @SecurityRequirement(name = "bearerAuth")
    public Response create(@Valid CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return Response.status(Response.Status.CREATED).entity(ApiResponse.success(created)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update Kategori")
    @SecurityRequirement(name = "bearerAuth")
    public Response update(@PathParam("id") Long id, @Valid CategoryRequest request) {
        return Response.ok(ApiResponse.success(categoryService.update(id, request))).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Hapus Kategori")
    @SecurityRequirement(name = "bearerAuth")
    public Response delete(@PathParam("id") Long id) {
        categoryService.delete(id);
        return Response.ok(ApiResponse.success(null)).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Statistik Kategori", description = "Jumlah produk per kategori (dengan cache)")
    public Response getStats() {
        List<Map<String, Object>> stats = categoryService.getCategoriesWithProductCount();
        return Response.ok(ApiResponse.success(stats)).build();
    }
}
