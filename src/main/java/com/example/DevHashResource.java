package com.example;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * 開発用ユーティリティAPI: BCryptハッシュ生成/検証
 * 開発モード専用の想定（本番では使用しない）
 */
@Path("/dev/hash")
@ApplicationScoped
@Produces(MediaType.TEXT_PLAIN)
public class DevHashResource {

    @GET
    @Path("/gen/{plain}")
    public Response generate(@PathParam("plain") String plain) {
        String hash = BcryptUtil.bcryptHash(plain);
        return Response.ok(hash).build();
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response verify(@FormParam("plain") String plain, @FormParam("hash") String hash) {
        boolean ok = BcryptUtil.matches(plain, hash);
        return Response.ok(Boolean.toString(ok)).build();
    }
}

