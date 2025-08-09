package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

/**
 * Favicon handler
 * - /favicon.ico へのアクセスを /favicon.svg にリダイレクトします。
 *   これにより自動リクエストでも 404/500 を避けられます。
 */
@Path("/")
public class FaviconResource {

    @GET
    @Path("/favicon.ico")
    public Response faviconIcoRedirect() {
        return Response.temporaryRedirect(URI.create("/favicon.svg")).build();
    }
}

