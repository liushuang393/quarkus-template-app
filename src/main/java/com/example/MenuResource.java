package com.example;

import java.util.List;
import java.util.Map;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/menu")
@Produces(MediaType.APPLICATION_JSON)
public class MenuResource {

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders headers;

    @Inject
    com.example.service.MessageService messageService;

    @GET
    @RolesAllowed({"ADMIN", "USER", "SALES"})
    public Map<String, Object> getMenu() {
        if (securityContext.isUserInRole("ADMIN")) {
            return Map.of(
                "role", "ADMIN",
                "menus", List.of(
                    Map.of("name", messageService.getMessage("menu.user.management", headers), "path", "/admin/users"),
                    Map.of("name", messageService.getMessage("menu.system.settings", headers), "path", "/admin/settings"),
                    Map.of("name", messageService.getMessage("menu.sales.management", headers), "path", "/sales"),
                    Map.of("name", messageService.getMessage("menu.reports", headers), "path", "/reports")
                )
            );
        } else if (securityContext.isUserInRole("SALES")) {
            return Map.of(
                "role", "SALES",
                "menus", List.of(
                    Map.of("name", messageService.getMessage("menu.sales.management", headers), "path", "/sales"),
                    Map.of("name", messageService.getMessage("menu.customer.management", headers), "path", "/customers"),
                    Map.of("name", messageService.getMessage("menu.reports", headers), "path", "/reports")
                )
            );
        } else {
            return Map.of(
                "role", "USER",
                "menus", List.of(
                    Map.of("name", messageService.getMessage("menu.profile", headers), "path", "/profile"),
                    Map.of("name", messageService.getMessage("menu.settings", headers), "path", "/settings")
                )
            );
        }
    }
}