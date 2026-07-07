package com.example.template.rest;

import com.example.template.dto.response.PostResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * MicroProfile REST Client menggantikan Spring Cloud OpenFeign.
 *
 * URL dikonfigurasi di application.properties:
 *   quarkus.rest-client.jsonplaceholder.url=https://jsonplaceholder.typicode.com
 *
 * Inject dengan @Inject @RestClient JsonPlaceholderClient client;
 */
@RegisterRestClient(configKey = "jsonplaceholder")
@Path("/posts")
public interface JsonPlaceholderClient {

    @GET
    List<PostResponse> getAllPosts();

    @GET
    @Path("/{id}")
    PostResponse getPostById(@PathParam("id") Long id);

    @GET
    List<PostResponse> getPostsByUserId(@QueryParam("userId") Long userId);
}
