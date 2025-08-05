# セキュリティ開発ガイド

このガイドでは、JWT認証とロールベースアクセス制御の実装について説明します。

## 🎯 セキュリティ要件

- **認証（Authentication）**: ユーザーの身元確認
- **認可（Authorization）**: リソースへのアクセス制御
- **JWT トークン管理**: セキュアなトークン生成・検証
- **パスワード暗号化**: 安全なパスワード保存
- **セッション管理**: トークンの有効期限管理

## 🔐 JWT認証実装

### JWT設定

```yaml
# application.yaml
mp:
  jwt:
    verify:
      publickey:
        location: META-INF/resources/publicKey.pem
      issuer: https://example.com
    decrypt:
      key:
        location: META-INF/resources/privateKey.pem

quarkus:
  smallrye-jwt:
    enabled: true
```

### JWTサービス実装

```java
package com.example.service;

import com.example.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class JwtService {
    
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;
    
    @ConfigProperty(name = "app.jwt.expiration.hours", defaultValue = "24")
    int expirationHours;
    
    /**
     * JWTトークン生成
     */
    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.getUsername())                    // ユーザープリンシパル名
                .groups(Set.of(user.getRole().name()))      // ロール
                .claim("userId", user.getId())              // ユーザーID
                .claim("email", user.getEmail())            // メールアドレス
                .claim("isActive", user.isActive())         // アクティブ状態
                .expiresIn(Duration.ofHours(expirationHours))
                .sign();
    }
    
    /**
     * リフレッシュトークン生成
     */
    public String generateRefreshToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.getUsername())
                .claim("type", "refresh")
                .claim("userId", user.getId())
                .expiresIn(Duration.ofDays(30))  // リフレッシュトークンは30日有効
                .sign();
    }
    
    /**
     * トークンからユーザーIDを取得
     */
    public Long getUserIdFromToken(JsonWebToken jwt) {
        return jwt.getClaim("userId");
    }
    
    /**
     * トークンの有効性チェック
     */
    public boolean isTokenValid(JsonWebToken jwt) {
        try {
            // 有効期限チェック
            if (jwt.getExpirationTime() < System.currentTimeMillis() / 1000) {
                return false;
            }
            
            // 発行者チェック
            if (!issuer.equals(jwt.getIssuer())) {
                return false;
            }
            
            // アクティブ状態チェック
            Boolean isActive = jwt.getClaim("isActive");
            return isActive != null && isActive;
            
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 認証エンドポイント

```java
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    
    @Inject
    UserService userService;
    
    @Inject
    JwtService jwtService;
    
    @Inject
    AuditLogService auditLogService;
    
    @Context
    HttpHeaders headers;
    
    /**
     * ログイン
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            Optional<User> userOpt = userService.authenticate(request.username, request.password);
            
            return userOpt.map(user -> {
                // JWTトークン生成
                String accessToken = jwtService.generateToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);
                
                // 監査ログ記録
                auditLogService.logSuccess(user.getId(), user.getUsername(), 
                    "USER_LOGIN", "User", user.getId().toString());
                
                return Response.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", 24 * 3600, // 24時間（秒）
                    "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                    )
                )).build();
                
            }).orElseGet(() -> {
                // 認証失敗
                auditLogService.logFailure(null, request.username, "USER_LOGIN", 
                    "User", null, "認証に失敗しました");
                
                return Response.status(401)
                    .entity(Map.of("error", "認証に失敗しました"))
                    .build();
            });
            
        } catch (Exception e) {
            LOG.error("ログインエラー", e);
            return Response.status(500)
                .entity(Map.of("error", "内部サーバーエラー"))
                .build();
        }
    }
    
    /**
     * トークンリフレッシュ
     */
    @POST
    @Path("/refresh")
    public Response refreshToken(@Valid RefreshTokenRequest request) {
        try {
            // リフレッシュトークンの検証は別途実装
            // ここでは簡略化
            
            Optional<User> userOpt = userService.findByUsername(request.username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String newAccessToken = jwtService.generateToken(user);
                
                return Response.ok(Map.of(
                    "accessToken", newAccessToken,
                    "tokenType", "Bearer",
                    "expiresIn", 24 * 3600
                )).build();
            }
            
            return Response.status(401)
                .entity(Map.of("error", "無効なリフレッシュトークン"))
                .build();
                
        } catch (Exception e) {
            return Response.status(401)
                .entity(Map.of("error", "トークンリフレッシュに失敗しました"))
                .build();
        }
    }
    
    /**
     * ログアウト
     */
    @POST
    @Path("/logout")
    @RolesAllowed({"ADMIN", "USER", "SALES"})
    public Response logout(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        
        // トークンブラックリスト登録（実装は省略）
        // tokenBlacklistService.addToBlacklist(token);
        
        // 監査ログ記録
        auditLogService.log(null, username, "USER_LOGOUT");
        
        return Response.ok(Map.of("message", "ログアウトしました")).build();
    }
}
```

## 🛡️ ロールベースアクセス制御

### ロール定義

```java
public enum Role {
    ADMIN("管理者", Set.of(
        Permission.USER_READ, Permission.USER_WRITE, Permission.USER_DELETE,
        Permission.SYSTEM_CONFIG, Permission.AUDIT_LOG_READ
    )),
    
    USER("一般ユーザー", Set.of(
        Permission.PROFILE_READ, Permission.PROFILE_WRITE
    )),
    
    SALES("営業", Set.of(
        Permission.USER_READ, Permission.CUSTOMER_READ, Permission.CUSTOMER_WRITE,
        Permission.SALES_READ, Permission.SALES_WRITE
    ));
    
    private final String displayName;
    private final Set<Permission> permissions;
    
    Role(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}

public enum Permission {
    USER_READ, USER_WRITE, USER_DELETE,
    PROFILE_READ, PROFILE_WRITE,
    CUSTOMER_READ, CUSTOMER_WRITE,
    SALES_READ, SALES_WRITE,
    SYSTEM_CONFIG, AUDIT_LOG_READ
}
```

### アクセス制御の実装

```java
@Path("/api/admin")
@RolesAllowed("ADMIN")
public class AdminController {
    
    /**
     * 管理者のみアクセス可能
     */
    @GET
    @Path("/users")
    public Response getAllUsers() {
        // 実装...
    }
    
    /**
     * システム設定（管理者のみ）
     */
    @PUT
    @Path("/config")
    public Response updateSystemConfig(@Valid SystemConfigRequest request) {
        // 実装...
    }
}

@Path("/api/users")
public class UserController {
    
    @Context
    SecurityContext securityContext;
    
    /**
     * 自分のプロフィール取得（全ロール）
     */
    @GET
    @Path("/profile")
    @RolesAllowed({"ADMIN", "USER", "SALES"})
    public Response getMyProfile() {
        String username = securityContext.getUserPrincipal().getName();
        // 実装...
    }
    
    /**
     * 他ユーザーのプロフィール取得（管理者と営業のみ）
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SALES"})
    public Response getUserProfile(@PathParam("id") Long id) {
        // 実装...
    }
    
    /**
     * 動的権限チェック
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "SALES"})
    public Response updateUser(@PathParam("id") Long id, @Valid UpdateUserRequest request) {
        String currentUsername = securityContext.getUserPrincipal().getName();
        
        // 自分自身の更新か管理者かチェック
        if (!securityContext.isUserInRole("ADMIN")) {
            User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new SecurityException("ユーザーが見つかりません"));
            
            if (!currentUser.getId().equals(id)) {
                throw new SecurityException("他のユーザーを更新する権限がありません");
            }
        }
        
        // 実装...
    }
}
```

### カスタム権限チェック

```java
@ApplicationScoped
public class SecurityService {
    
    @Inject
    UserService userService;
    
    /**
     * リソースアクセス権限チェック
     */
    public boolean hasResourceAccess(String username, String resourceType, String resourceId, String action) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // 管理者は全てのリソースにアクセス可能
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        
        // リソース固有の権限チェック
        switch (resourceType) {
            case "USER":
                return checkUserResourceAccess(user, resourceId, action);
            case "CUSTOMER":
                return checkCustomerResourceAccess(user, resourceId, action);
            default:
                return false;
        }
    }
    
    private boolean checkUserResourceAccess(User user, String resourceId, String action) {
        // 自分自身のリソースは読み取り・更新可能
        if (user.getId().toString().equals(resourceId)) {
            return "READ".equals(action) || "UPDATE".equals(action);
        }
        
        // 営業は他のユーザーを読み取り可能
        if (user.getRole() == User.Role.SALES && "READ".equals(action)) {
            return true;
        }
        
        return false;
    }
    
    private boolean checkCustomerResourceAccess(User user, String resourceId, String action) {
        // 営業のみ顧客リソースにアクセス可能
        return user.getRole() == User.Role.SALES;
    }
}
```

## 🔒 パスワードセキュリティ

### パスワード暗号化

```java
@ApplicationScoped
public class PasswordService {
    
    @ConfigProperty(name = "app.password.bcrypt.rounds", defaultValue = "12")
    int bcryptRounds;
    
    /**
     * パスワードハッシュ化
     */
    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword, bcryptRounds);
    }
    
    /**
     * パスワード検証
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
    
    /**
     * パスワード強度チェック
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return PasswordStrength.WEAK;
        }
        
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        int score = 0;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        if (password.length() >= 12) score++;
        
        if (score >= 4) return PasswordStrength.STRONG;
        if (score >= 2) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
    
    /**
     * 一時パスワード生成
     */
    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
```

### パスワードバリデーション

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "パスワードは8文字以上で、大文字、小文字、数字を含む必要があります";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@ApplicationScoped
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    @Inject
    PasswordService passwordService;
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        
        PasswordService.PasswordStrength strength = passwordService.checkPasswordStrength(password);
        return strength != PasswordService.PasswordStrength.WEAK;
    }
}
```

## 🛡️ セキュリティフィルター

### JWTフィルター

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    
    @Inject
    JwtService jwtService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // 認証不要のパスをスキップ
        if (isPublicPath(path)) {
            return;
        }
        
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(401)
                    .entity(Map.of("error", "認証トークンが必要です"))
                    .build()
            );
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // トークン検証はQuarkusが自動で行う
            // 追加の検証が必要な場合はここで実装
            
        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(401)
                    .entity(Map.of("error", "無効なトークンです"))
                    .build()
            );
        }
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/") || 
               path.startsWith("/health") || 
               path.startsWith("/q/");
    }
}
```

## 🔐 セキュリティ設定

### CORS設定

```java
@ApplicationScoped
public class CorsFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Content-Type, Authorization");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }
}
```

### セキュリティヘッダー

```java
@ApplicationScoped
public class SecurityHeadersFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        // XSS保護
        responseContext.getHeaders().add("X-XSS-Protection", "1; mode=block");
        
        // コンテンツタイプスニッフィング防止
        responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
        
        // フレーム埋め込み防止
        responseContext.getHeaders().add("X-Frame-Options", "DENY");
        
        // HTTPS強制（本番環境）
        responseContext.getHeaders().add("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        // CSP設定
        responseContext.getHeaders().add("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
    }
}
```

## 🧪 セキュリティテスト

### 認証テスト

```java
@QuarkusTest
class AuthControllerTest {
    
    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.username = "admin";
        request.password = "password";
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/auth/login")
            .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("user.username", equalTo("admin"));
    }
    
    @Test
    void testLoginFailure() {
        LoginRequest request = new LoginRequest();
        request.username = "admin";
        request.password = "wrongpassword";
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/auth/login")
            .then()
            .statusCode(401)
            .body("error", notNullValue());
    }
    
    @Test
    void testProtectedEndpointWithoutToken() {
        given()
            .when().get("/api/users/profile")
            .then()
            .statusCode(401);
    }
    
    @Test
    void testProtectedEndpointWithValidToken() {
        String token = getValidToken();
        
        given()
            .header("Authorization", "Bearer " + token)
            .when().get("/api/users/profile")
            .then()
            .statusCode(200);
    }
    
    private String getValidToken() {
        // テスト用トークン取得
        return "valid-jwt-token";
    }
}
```

---

このガイドを参考に、セキュアなアプリケーションを構築してください。
