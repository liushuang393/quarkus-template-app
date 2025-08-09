package com.example;

import com.example.mapper.UserMapper;
import com.example.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

/** 開発用: ユーザーのハッシュ確認/検証エンドポイント（dev専用） */
@Path("/dev/user")
@Produces(MediaType.TEXT_PLAIN)
public class DevUserResource {

    @Inject
    UserMapper userMapper;

    @GET
    @Path("/hash/{username}")
    public Response getHash(@PathParam("username") String username) {
        Optional<User> u = userMapper.findByUsername(username);
        if (u.isEmpty()) {
            return Response.status(404).entity("not found").build();
        }
        return Response.ok(u.get().getPassword()).build();
    }

    @GET
    @Path("/verify/{username}/{plain}")
    public Response verify(@PathParam("username") String username, @PathParam("plain") String plain) {
        Optional<User> u = userMapper.findByUsername(username);
        if (u.isEmpty()) {
            return Response.status(404).entity("not found").build();
        }
        boolean ok = BcryptUtil.matches(plain, u.get().getPassword());
        return Response.ok(Boolean.toString(ok)).build();
    }
}

