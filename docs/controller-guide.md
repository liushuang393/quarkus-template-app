# コントローラー層開発ガイド

このガイドでは、Quarkus REST（JAX-RS）を使用したコントローラー層の実装について説明します。

## 🎯 コントローラー層の役割

- **HTTPリクエストの受信**: クライアントからのリクエストを受け取る
- **入力検証**: リクエストデータのバリデーション
- **サービス層呼び出し**: ビジネスロジックの実行
- **レスポンス生成**: 適切なHTTPレスポンスを返す
- **エラーハンドリング**: 例外の適切な処理

## 📁 ファイル構成

```
src/main/java/com/example/
├── controller/
│   ├── AuthController.java      # 認証関連API
│   ├── UserController.java      # ユーザー管理API
│   └── MenuController.java      # メニューAPI
├── dto/                         # データ転送オブジェクト
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── ErrorResponse.java
└── exception/                   # 例外処理
    └── GlobalExceptionMapper.java
```

## 🔧 基本実装

### コントローラークラスの基本構造

```java
package com.example.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "ユーザー管理", description = "ユーザー関連のAPI")
public class UserController {

    @Inject
    UserService userService;
    
    @Inject
    MessageService messageService;
    
    @Context
    HttpHeaders headers;
    
    // メソッド実装...
}
```

### GET エンドポイント

```java
/**
 * ユーザー一覧取得
 */
@GET
@Operation(summary = "ユーザー一覧取得", description = "全ユーザーの一覧を取得します")
@APIResponse(responseCode = "200", description = "取得成功")
public Response getUsers() {
    try {
        List<User> users = userService.findAll();
        return Response.ok(users).build();
    } catch (Exception e) {
        LOG.error("ユーザー一覧取得エラー", e);
        return Response.status(500)
            .entity(Map.of("error", "内部サーバーエラー"))
            .build();
    }
}

/**
 * ユーザー詳細取得
 */
@GET
@Path("/{id}")
@Operation(summary = "ユーザー詳細取得")
public Response getUser(@PathParam("id") Long id) {
    Optional<User> user = userService.findById(id);
    
    if (user.isPresent()) {
        return Response.ok(user.get()).build();
    } else {
        String message = messageService.getMessage("error.user.not.found", headers);
        return Response.status(404)
            .entity(Map.of("error", message))
            .build();
    }
}

/**
 * クエリパラメータを使用した検索
 */
@GET
@Path("/search")
@Operation(summary = "ユーザー検索")
public Response searchUsers(
    @QueryParam("username") String username,
    @QueryParam("role") String role,
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("10") int size) {
    
    SearchCriteria criteria = new SearchCriteria(username, role, page, size);
    PagedResult<User> result = userService.search(criteria);
    
    return Response.ok(result).build();
}
```

### POST エンドポイント

```java
/**
 * ユーザー作成
 */
@POST
@Operation(summary = "ユーザー作成", description = "新しいユーザーを作成します")
@APIResponse(responseCode = "201", description = "作成成功")
@APIResponse(responseCode = "400", description = "バリデーションエラー")
public Response createUser(@Valid CreateUserRequest request) {
    try {
        User user = userService.create(request, headers);
        
        return Response.status(201)
            .entity(Map.of(
                "message", messageService.getMessage("user.created.success", headers),
                "userId", user.getId()
            ))
            .build();
            
    } catch (BusinessException e) {
        return Response.status(400)
            .entity(Map.of("error", e.getMessage()))
            .build();
    }
}

/**
 * ファイルアップロード
 */
@POST
@Path("/upload")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Operation(summary = "プロフィール画像アップロード")
public Response uploadProfileImage(
    @FormParam("file") InputStream fileInputStream,
    @FormParam("file") FormDataContentDisposition fileDetail) {
    
    try {
        String fileName = fileService.saveFile(fileInputStream, fileDetail);
        
        return Response.ok(Map.of(
            "message", "ファイルアップロード成功",
            "fileName", fileName
        )).build();
        
    } catch (Exception e) {
        return Response.status(500)
            .entity(Map.of("error", "ファイルアップロードに失敗しました"))
            .build();
    }
}
```

### PUT エンドポイント

```java
/**
 * ユーザー更新
 */
@PUT
@Path("/{id}")
@Operation(summary = "ユーザー更新")
public Response updateUser(
    @PathParam("id") Long id,
    @Valid UpdateUserRequest request) {
    
    try {
        User updatedUser = userService.update(id, request, headers);
        
        return Response.ok(Map.of(
            "message", messageService.getMessage("user.updated.success", headers),
            "user", updatedUser
        )).build();
        
    } catch (BusinessException e) {
        return Response.status(400)
            .entity(Map.of("error", e.getMessage()))
            .build();
    } catch (NotFoundException e) {
        return Response.status(404)
            .entity(Map.of("error", e.getMessage()))
            .build();
    }
}
```

### DELETE エンドポイント

```java
/**
 * ユーザー削除
 */
@DELETE
@Path("/{id}")
@Operation(summary = "ユーザー削除")
public Response deleteUser(@PathParam("id") Long id) {
    try {
        userService.delete(id);
        
        return Response.ok(Map.of(
            "message", messageService.getMessage("user.deleted.success", headers)
        )).build();
        
    } catch (NotFoundException e) {
        return Response.status(404)
            .entity(Map.of("error", e.getMessage()))
            .build();
    }
}

/**
 * 論理削除（無効化）
 */
@PUT
@Path("/{id}/deactivate")
@Operation(summary = "ユーザー無効化")
public Response deactivateUser(@PathParam("id") Long id) {
    try {
        userService.deactivate(id);
        
        return Response.ok(Map.of(
            "message", messageService.getMessage("user.deactivated.success", headers)
        )).build();
        
    } catch (NotFoundException e) {
        return Response.status(404)
            .entity(Map.of("error", e.getMessage()))
            .build();
    }
}
```

## 🔒 セキュリティ実装

### JWT認証

```java
@Path("/api/secure")
@RolesAllowed({"ADMIN", "USER"})
public class SecureController {

    @Context
    SecurityContext securityContext;
    
    @GET
    @Path("/profile")
    @RolesAllowed("USER")
    public Response getProfile() {
        String username = securityContext.getUserPrincipal().getName();
        // 実装...
    }
    
    @GET
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public Response getAdminData() {
        // 管理者のみアクセス可能
        // 実装...
    }
}
```

### CORS設定

```java
@Path("/api/public")
public class PublicController {

    @GET
    @Path("/health")
    @CrossOrigin(origins = "*", methods = {"GET"})
    public Response healthCheck() {
        return Response.ok(Map.of("status", "OK")).build();
    }
}
```

## 📝 バリデーション

### Bean Validation

```java
// DTOクラス
public class CreateUserRequest {
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以下です")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "ユーザー名は英数字とアンダースコアのみ使用可能です")
    public String username;
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以下です")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "パスワードは大文字、小文字、数字を含む必要があります")
    public String password;
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    public String email;
    
    @NotNull(message = "ロールは必須です")
    public User.Role role;
}

// コントローラーでの使用
@POST
public Response createUser(@Valid CreateUserRequest request) {
    // @Validアノテーションにより自動的にバリデーションが実行される
    // バリデーションエラーはConstraintViolationExceptionとして投げられる
}
```

### カスタムバリデーション

```java
// カスタムバリデーターアノテーション
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface UniqueUsername {
    String message() default "ユーザー名が既に存在します";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// バリデーター実装
@ApplicationScoped
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    
    @Inject
    UserService userService;
    
    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return username == null || !userService.existsByUsername(username);
    }
}
```

## 📊 OpenAPI/Swagger ドキュメント

### API ドキュメント設定

```java
@Path("/api/users")
@Tag(name = "ユーザー管理", description = "ユーザー関連のAPI操作")
public class UserController {

    @GET
    @Path("/{id}")
    @Operation(
        summary = "ユーザー詳細取得",
        description = "指定されたIDのユーザー詳細情報を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "取得成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = User.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "ユーザーが見つかりません",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    public Response getUser(
        @Parameter(description = "ユーザーID", required = true)
        @PathParam("id") Long id) {
        // 実装...
    }
}
```

## 🔧 エラーハンドリング

### グローバル例外ハンドラー

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    
    @Inject
    MessageService messageService;
    
    @Context
    HttpHeaders headers;
    
    @Override
    public Response toResponse(Exception exception) {
        
        // バリデーション例外
        if (exception instanceof ConstraintViolationException) {
            return handleValidationException((ConstraintViolationException) exception);
        }
        
        // ビジネス例外
        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception);
        }
        
        // その他の例外
        LOG.error("予期しないエラー", exception);
        String message = messageService.getMessage("error.internal.server.error", headers);
        
        return Response.status(500)
            .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", message))
            .build();
    }
    
    private Response handleValidationException(ConstraintViolationException e) {
        List<FieldError> fieldErrors = e.getConstraintViolations()
            .stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());
            
        String message = messageService.getMessage("error.validation.error", headers);
        ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", message, fieldErrors);
        
        return Response.status(400).entity(errorResponse).build();
    }
}
```

## 🧪 テスト実装

### 単体テスト

```java
@QuarkusTest
class UserControllerTest {

    @Test
    void testGetUser() {
        given()
            .when().get("/api/users/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("username", notNullValue());
    }
    
    @Test
    void testCreateUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.username = "testuser";
        request.password = "TestPass123";
        request.email = "test@example.com";
        request.role = User.Role.USER;
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/api/users")
            .then()
            .statusCode(201)
            .body("message", notNullValue())
            .body("userId", notNullValue());
    }
    
    @Test
    void testGetUserNotFound() {
        given()
            .when().get("/api/users/999")
            .then()
            .statusCode(404)
            .body("error", notNullValue());
    }
}
```

## 📚 ベストプラクティス

### 1. RESTful設計

```java
// 良い例
GET    /api/users           # ユーザー一覧
GET    /api/users/{id}      # ユーザー詳細
POST   /api/users           # ユーザー作成
PUT    /api/users/{id}      # ユーザー更新
DELETE /api/users/{id}      # ユーザー削除

// 悪い例
GET    /api/getUsers
POST   /api/createUser
POST   /api/updateUser
POST   /api/deleteUser
```

### 2. 適切なHTTPステータスコード

```java
// 成功
return Response.ok(data).build();                    // 200 OK
return Response.status(201).entity(data).build();   // 201 Created
return Response.noContent().build();                // 204 No Content

// クライアントエラー
return Response.status(400).entity(error).build();  // 400 Bad Request
return Response.status(401).entity(error).build();  // 401 Unauthorized
return Response.status(403).entity(error).build();  // 403 Forbidden
return Response.status(404).entity(error).build();  // 404 Not Found

// サーバーエラー
return Response.status(500).entity(error).build();  // 500 Internal Server Error
```

### 3. レスポンス形式の統一

```java
// 成功レスポンス
{
    "data": { ... },
    "message": "操作が成功しました"
}

// エラーレスポンス
{
    "error": "エラーメッセージ",
    "code": "ERROR_CODE",
    "details": [ ... ]
}
```

---

このガイドを参考に、保守性が高く、拡張しやすいコントローラー層を実装してください。
