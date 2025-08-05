package com.example.service;

import com.example.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.getUsername())
                .groups(user.getRole().name())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .expiresIn(Duration.ofHours(24))
                .sign();
    }
}