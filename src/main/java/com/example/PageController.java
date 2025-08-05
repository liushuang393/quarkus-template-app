package com.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * ページコントローラー - Quteテンプレートを使用してページを表示
 */
@Path("/")
public class PageController {

    @Inject
    Template login;

    @Inject
    Template dashboard;

    @Inject
    com.example.service.MessageService messageService;

    @Context
    HttpHeaders headers;

    /**
     * ログインページを表示
     * @param lang 言語パラメータ（オプション）
     * @return ログインページのHTML
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index(@QueryParam("lang") String lang) {
        Locale locale = getLocale(lang);
        return login.data("messages", getMessages(locale));
    }

    /**
     * ログインページを表示（/loginパス）
     * @param lang 言語パラメータ（オプション）
     * @return ログインページのHTML
     */
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage(@QueryParam("lang") String lang) {
        Locale locale = getLocale(lang);
        return login.data("messages", getMessages(locale));
    }

    /**
     * 言語パラメータまたはAccept-Languageヘッダーからロケールを取得
     */
    private Locale getLocale(String lang) {
        if (lang != null && !lang.trim().isEmpty()) {
            return messageService.parseLocale(lang);
        }
        return messageService.parseLocale(headers.getHeaderString("Accept-Language"));
    }

    /**
     * テンプレート用のメッセージマップを作成
     */
    private Map<String, String> getMessages(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        messages.put("title", messageService.getMessage("auth.login.title", locale));
        messages.put("header", messageService.getMessage("auth.login.header", locale));
        messages.put("description", messageService.getMessage("auth.login.description", locale));
        messages.put("username", messageService.getMessage("auth.login.username", locale));
        messages.put("password", messageService.getMessage("auth.login.password", locale));
        messages.put("loginButton", messageService.getMessage("auth.login.button", locale));
        messages.put("loginToggle", messageService.getMessage("auth.login.toggle", locale));
        messages.put("registerUsername", messageService.getMessage("auth.register.username", locale));
        messages.put("registerEmail", messageService.getMessage("auth.register.email", locale));
        messages.put("registerPassword", messageService.getMessage("auth.register.password", locale));
        messages.put("registerRole", messageService.getMessage("auth.register.role", locale));
        messages.put("registerButton", messageService.getMessage("auth.register.button", locale));
        messages.put("registerToggle", messageService.getMessage("auth.register.toggle", locale));
        messages.put("menuTitle", messageService.getMessage("menu.title", locale));
        messages.put("menuUser", messageService.getMessage("menu.user", locale));
        messages.put("menuRole", messageService.getMessage("menu.role", locale));
        messages.put("menuLogout", messageService.getMessage("menu.logout", locale));
        messages.put("roleAdmin", messageService.getMessage("role.admin", locale));
        messages.put("roleUser", messageService.getMessage("role.user", locale));
        messages.put("roleSales", messageService.getMessage("role.sales", locale));
        return messages;
    }

    /**
     * ダッシュボードページを表示
     * @param lang 言語パラメータ（オプション）
     * @return ダッシュボードページのHTML
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance dashboardPage(@QueryParam("lang") String lang) {
        Locale locale = getLocale(lang);
        Map<String, String> messages = getMessages(locale);

        // ダッシュボード固有のメッセージを追加
        messages.put("dashboardTitle", messageService.getMessage("dashboard.title", locale));
        messages.put("dashboard", messageService.getMessage("dashboard.dashboard", locale));
        messages.put("home", messageService.getMessage("dashboard.home", locale));
        messages.put("refresh", messageService.getMessage("dashboard.refresh", locale));
        messages.put("totalUsers", messageService.getMessage("dashboard.total.users", locale));
        messages.put("activeUsers", messageService.getMessage("dashboard.active.users", locale));
        messages.put("todayLogins", messageService.getMessage("dashboard.today.logins", locale));
        messages.put("systemStatus", messageService.getMessage("dashboard.system.status", locale));
        messages.put("online", messageService.getMessage("dashboard.online", locale));
        messages.put("recentActivity", messageService.getMessage("dashboard.recent.activity", locale));
        messages.put("time", messageService.getMessage("dashboard.time", locale));
        messages.put("user", messageService.getMessage("dashboard.user", locale));
        messages.put("action", messageService.getMessage("dashboard.action", locale));
        messages.put("status", messageService.getMessage("dashboard.status", locale));
        messages.put("quickActions", messageService.getMessage("dashboard.quick.actions", locale));
        messages.put("addUser", messageService.getMessage("dashboard.add.user", locale));
        messages.put("viewReports", messageService.getMessage("dashboard.view.reports", locale));
        messages.put("systemSettings", messageService.getMessage("dashboard.system.settings", locale));
        messages.put("profile", messageService.getMessage("dashboard.profile", locale));
        messages.put("settings", messageService.getMessage("dashboard.settings", locale));
        messages.put("logout", messageService.getMessage("dashboard.logout", locale));

        return dashboard.data("messages", messages);
    }
}
