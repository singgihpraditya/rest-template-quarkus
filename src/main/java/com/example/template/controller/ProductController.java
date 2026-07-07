package com.example.template.controller;

import com.example.template.aspect.Logged;
import com.example.template.dto.request.ProductRequest;
import com.example.template.dto.response.ApiResponse;
import com.example.template.dto.response.PageResponse;
import com.example.template.dto.response.ProductResponse;
import com.example.template.service.ProductService;
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

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product", description = "Manajemen produk")
@Logged
@Slf4j
public class ProductController {

    @Inject
    ProductService productService;

    @GET
    @Operation(summary = "List Produk", description = "Ambil semua produk dengan pagination")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("search") String search) {
        PageResponse<ProductResponse> result = productService.findAll(page, size, search);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail Produk")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(productService.findById(id))).build();
    }

    @GET
    @Path("/category/{categoryId}")
    @Operation(summary = "Produk by Kategori")
    public Response getByCategory(
            @PathParam("categoryId") Long categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(ApiResponse.success(productService.findByCategory(categoryId, page, size))).build();
    }

    @GET
    @Path("/top")
    @Operation(summary = "Top Produk by Harga", description = "Ambil produk termahal (native query)")
    public Response getTopByPrice(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<ProductResponse> products = productService.getTopProductsByPrice(limit);
        return Response.ok(ApiResponse.success(products)).build();
    }

    @GET
    @Path("/tag/{tagName}")
    @Operation(summary = "Produk by Tag", description = "Cari produk berdasarkan nama tag (native query)")
    public Response getByTag(@PathParam("tagName") String tagName) {
        List<ProductResponse> products = productService.findByTag(tagName);
        return Response.ok(ApiResponse.success(products)).build();
    }

    @POST
    @Operation(summary = "Tambah Produk")
    @SecurityRequirement(name = "bearerAuth")
    public Response create(@Valid ProductRequest request) {
        ProductResponse created = productService.create(request);
        return Response.status(Response.Status.CREATED).entity(ApiResponse.success(created)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update Produk")
    @SecurityRequirement(name = "bearerAuth")
    public Response update(@PathParam("id") Long id, @Valid ProductRequest request) {
        return Response.ok(ApiResponse.success(productService.update(id, request))).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Hapus Produk")
    @SecurityRequirement(name = "bearerAuth")
    public Response delete(@PathParam("id") Long id) {
        productService.delete(id);
        return Response.ok(ApiResponse.success(null)).build();
    }
}
