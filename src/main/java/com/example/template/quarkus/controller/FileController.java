package com.example.template.quarkus.controller;

import com.example.template.quarkus.aspect.Logged;
import com.example.template.quarkus.dto.response.ApiResponse;
import com.example.template.quarkus.dto.response.ErrorSchema;
import com.example.template.quarkus.service.FileStorageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/files")
@Tag(name = "File", description = "Upload dan download file")
@SecurityRequirement(name = "bearerAuth")
@Logged
@Slf4j
public class FileController {

    @Inject
    FileStorageService fileStorageService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload File")
    public Response uploadFile(
            @RestForm("file") FileUpload fileUpload,
            @Context UriInfo uriInfo) throws IOException {

        if (fileUpload == null || fileUpload.fileName() == null || fileUpload.fileName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(ErrorSchema.validationError("File tidak boleh kosong", "File tidak boleh kosong")))
                    .build();
        }

        String originalFilename = fileUpload.fileName();
        java.nio.file.Path uploadedPath = fileUpload.uploadedFile();

        String storedFilename;
        try (InputStream is = Files.newInputStream(uploadedPath)) {
            storedFilename = fileStorageService.storeFile(originalFilename, is);
        }

        String downloadUrl = uriInfo.getBaseUri() + "api/files/download/" + storedFilename;

        Map<String, String> result = new LinkedHashMap<>();
        result.put("filename", storedFilename);
        result.put("original_filename", originalFilename);
        result.put("download_url", downloadUrl);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success(result))
                .build();
    }

    @GET
    @Path("/download/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download File")
    public Response downloadFile(@PathParam("filename") String filename) {
        java.nio.file.Path filePath = fileStorageService.loadFileAsPath(filename);
        File file = filePath.toFile();

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return Response.ok(file)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .type(contentType)
                .build();
    }
}
