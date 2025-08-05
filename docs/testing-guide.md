# テスト開発ガイド

このガイドでは、単体テストと統合テストの実装方法について説明します。

## 🎯 テスト戦略

- **単体テスト**: 個別のクラス・メソッドのテスト
- **統合テスト**: 複数のコンポーネント間の連携テスト
- **E2Eテスト**: エンドツーエンドの機能テスト
- **パフォーマンステスト**: 性能要件の検証

## 📁 テスト構成

```
src/test/java/com/example/
├── controller/              # コントローラーテスト
│   ├── AuthControllerTest.java
│   └── UserControllerTest.java
├── service/                 # サービステスト
│   ├── UserServiceTest.java
│   └── MessageServiceTest.java
├── mapper/                  # マッパーテスト
│   ├── UserMapperTest.java
│   └── AuditLogMapperTest.java
└── integration/             # 統合テスト
    └── AuthIntegrationTest.java

src/test/resources/
├── application.properties   # テスト設定
└── test-data.sql           # テストデータ
```

## 🔧 テスト環境設定

### application.properties（テスト用）

```properties
# データベース設定（H2インメモリ）
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
quarkus.datasource.username=sa
quarkus.datasource.password=

# MyBatis設定
quarkus.mybatis.mapper-locations=classpath*:mapper/*.xml
quarkus.mybatis.configuration.map-underscore-to-camel-case=true

# JWT設定（テスト用）
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
mp.jwt.verify.issuer=https://test.example.com

# ログレベル
quarkus.log.level=INFO
quarkus.log.category."com.example".level=DEBUG

# テスト専用設定
app.test.mode=true
```

## 🧪 単体テスト

### サービス層テスト

```java
@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserMapper userMapper;

    @InjectMock
    MessageService messageService;

    @InjectMock
    AuditLogService auditLogService;

    @Test
    void testCreateUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "testuser";
        request.password = "TestPass123";
        request.email = "test@example.com";
        request.role = User.Role.USER;

        when(userMapper.findByUsername("testuser"))
            .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // IDを設定
            return null;
        }).when(userMapper).insert(any(User.class));

        // When
        User result = userService.create(request, null);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(User.Role.USER, result.getRole());
        assertTrue(BcryptUtil.matches("TestPass123", result.getPassword()));

        verify(userMapper).findByUsername("testuser");
        verify(userMapper).insert(any(User.class));
        verify(auditLogService).logSuccess(eq(1L), eq("testuser"),
            eq("USER_CREATE"), eq("User"), eq("1"));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "existing";

        User existingUser = new User();
        existingUser.setUsername("existing");

        when(userMapper.findByUsername("existing"))
            .thenReturn(Optional.of(existingUser));
        when(messageService.getMessage(eq("error.user.already.exists"), any()))
            .thenReturn("ユーザー名が既に存在します");

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> userService.create(request, null)
        );

        assertEquals("USER_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("ユーザー名が既に存在します", exception.getMessage());

        verify(userMapper).findByUsername("existing");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testAuthenticate_Success() {
        // Given
        String username = "testuser";
        String password = "TestPass123";
        String hashedPassword = BcryptUtil.bcryptHash(password);

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setActive(true);

        when(userMapper.findActiveByUsername(username))
            .thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticate(username, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());

        verify(userMapper).findActiveByUsername(username);
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        // Given
        String username = "testuser";
        String password = "WrongPass";
        String hashedPassword = BcryptUtil.bcryptHash("CorrectPass");

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setActive(true);

        when(userMapper.findActiveByUsername(username))
            .thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticate(username, password);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Given
        when(userMapper.findActiveByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.authenticate("nonexistent", "password");

        // Then
        assertFalse(result.isPresent());
    }
}
```

### マッパー層テスト

```java
@QuarkusTest
@TestTransaction
class UserMapperTest {

    @Inject
    UserMapper userMapper;

    @Test
    void testInsertAndFindById() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER);

        // When
        userMapper.insert(user);

        // Then
        assertNotNull(user.getId());

        Optional<User> found = userMapper.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals(User.Role.USER, found.get().getRole());
        assertTrue(found.get().isActive());
    }

    @Test
    void testFindByUsername() {
        // Given
        User user = createTestUser("findtest", "find@test.com");
        userMapper.insert(user);

        // When
        Optional<User> found = userMapper.findByUsername("findtest");

        // Then
        assertTrue(found.isPresent());
        assertEquals("findtest", found.get().getUsername());
    }

    @Test
    void testUpdate() {
        // Given
        User user = createTestUser("updatetest", "update@test.com");
        userMapper.insert(user);

        // When
        user.setEmail("updated@test.com");
        user.setRole(User.Role.ADMIN);
        int updated = userMapper.update(user);

        // Then
        assertEquals(1, updated);

        Optional<User> found = userMapper.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("updated@test.com", found.get().getEmail());
        assertEquals(User.Role.ADMIN, found.get().getRole());
    }

    @Test
    void testDeleteById() {
        // Given
        User user = createTestUser("deletetest", "delete@test.com");
        userMapper.insert(user);

        // When
        int deleted = userMapper.deleteById(user.getId());

        // Then
        assertEquals(1, deleted);

        Optional<User> found = userMapper.findById(user.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testSearch() {
        // Given
        insertTestUsers();

        // When
        List<User> results = userMapper.search(
            "search", null, User.Role.USER, true,
            null, null, "username", "ASC", 10, 0
        );

        // Then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(u -> u.getUsername().contains("search")));
        assertTrue(results.stream().allMatch(u -> u.getRole() == User.Role.USER));
    }

    @Test
    void testCount() {
        // Given
        long initialCount = userMapper.count();
        insertTestUsers();

        // When
        long newCount = userMapper.count();

        // Then
        assertTrue(newCount > initialCount);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("hashedpassword");
        user.setEmail(email);
        user.setRole(User.Role.USER);
        return user;
    }

    private void insertTestUsers() {
        for (int i = 1; i <= 5; i++) {
            User user = createTestUser("searchuser" + i, "search" + i + "@test.com");
            userMapper.insert(user);
        }
    }
}
```

## 🌐 統合テスト

### REST APIテスト

```java
@QuarkusTest
class AuthControllerTest {

    @Test
    void testLogin_Success() {
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
            .body("tokenType", equalTo("Bearer"))
            .body("user.username", equalTo("admin"))
            .body("user.role", equalTo("ADMIN"));
    }

    @Test
    void testLogin_InvalidCredentials() {
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
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.username = "newuser";
        request.password = "NewPass123";
        request.email = "new@example.com";
        request.role = User.Role.USER;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/auth/register")
            .then()
            .statusCode(200)
            .body("message", notNullValue())
            .body("userId", notNullValue());
    }

    @Test
    void testRegister_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.username = "admin"; // 既存ユーザー
        request.password = "NewPass123";
        request.email = "duplicate@example.com";
        request.role = User.Role.USER;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/auth/register")
            .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    void testProtectedEndpoint_WithoutToken() {
        given()
            .when().get("/api/users/profile")
            .then()
            .statusCode(401);
    }

    @Test
    void testProtectedEndpoint_WithValidToken() {
        String token = getValidToken();

        given()
            .header("Authorization", "Bearer " + token)
            .when().get("/menu")
            .then()
            .statusCode(200)
            .body("role", notNullValue())
            .body("menus", notNullValue());
    }

    @Test
    void testProtectedEndpoint_WithInvalidToken() {
        given()
            .header("Authorization", "Bearer invalid-token")
            .when().get("/menu")
            .then()
            .statusCode(401);
    }

    private String getValidToken() {
        LoginRequest request = new LoginRequest();
        request.username = "admin";
        request.password = "password";

        return given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("accessToken");
    }
}
```

### データベース統合テスト

```java
@QuarkusTest
@TestTransaction
class DatabaseIntegrationTest {

    @Inject
    UserService userService;

    @Inject
    AuditLogService auditLogService;

    @Test
    void testUserCreationWithAuditLog() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "integrationtest";
        request.password = "TestPass123";
        request.email = "integration@test.com";
        request.role = User.Role.USER;

        // When
        User user = userService.create(request, null);

        // Then
        assertNotNull(user.getId());
        assertEquals("integrationtest", user.getUsername());

        // 監査ログが記録されているかチェック
        // 実際の実装では監査ログ検索メソッドを使用
        // List<AuditLog> logs = auditLogService.findByUserId(user.getId());
        // assertFalse(logs.isEmpty());
        // assertEquals("USER_CREATE", logs.get(0).getAction());
    }

    @Test
    void testTransactionRollback() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "rollbacktest";
        request.password = "TestPass123";
        request.email = "rollback@test.com";
        request.role = User.Role.USER;

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            // トランザクション内で例外を発生させる
            userService.createWithException(request);
        });

        // ロールバックされているかチェック
        Optional<User> user = userService.findByUsername("rollbacktest");
        assertFalse(user.isPresent());
    }
}
```

## 🎭 モックとスタブ

### Mockitoを使用したモック

```java
@QuarkusTest
class UserServiceMockTest {

    @InjectMock
    UserMapper userMapper;

    @InjectMock
    MessageService messageService;

    @Inject
    UserService userService;

    @Test
    void testCreateUser_WithMocks() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "mocktest";
        request.password = "TestPass123";
        request.email = "mock@test.com";
        request.role = User.Role.USER;

        when(userMapper.findByUsername("mocktest"))
            .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return null;
        }).when(userMapper).insert(any(User.class));

        // When
        User result = userService.create(request, null);

        // Then
        assertEquals(100L, result.getId());
        assertEquals("mocktest", result.getUsername());

        verify(userMapper, times(1)).findByUsername("mocktest");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(userMapper.findById(999L))
            .thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userMapper).findById(999L);
    }
}
```

## 📊 テストデータ管理

### テストデータファイル

```sql
-- test-data.sql
INSERT INTO users (id, username, password, email, role, created_at, is_active) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'admin@example.com', 'ADMIN', CURRENT_TIMESTAMP, true),
(2, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'user1@example.com', 'USER', CURRENT_TIMESTAMP, true),
(3, 'sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'sales1@example.com', 'SALES', CURRENT_TIMESTAMP, true);
```

### テストデータビルダー

```java
public class TestDataBuilder {

    public static User.UserBuilder defaultUser() {
        return User.builder()
            .username("testuser")
            .password(BcryptUtil.bcryptHash("TestPass123"))
            .email("test@example.com")
            .role(User.Role.USER)
            .active(true)
            .createdAt(LocalDateTime.now());
    }

    public static CreateUserRequest.CreateUserRequestBuilder defaultCreateRequest() {
        return CreateUserRequest.builder()
            .username("testuser")
            .password("TestPass123")
            .email("test@example.com")
            .role(User.Role.USER);
    }

    public static LoginRequest.LoginRequestBuilder defaultLoginRequest() {
        return LoginRequest.builder()
            .username("testuser")
            .password("TestPass123");
    }
}

// 使用例
@Test
void testWithBuilder() {
    User user = TestDataBuilder.defaultUser()
        .username("customuser")
        .email("custom@example.com")
        .build();

    // テスト実行...
}
```

## 🚀 パフォーマンステスト

### JMeterテスト設定

```xml
<!-- jmeter-test-plan.jmx -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="API Performance Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
    </TestPlan>

    <!-- Thread Group -->
    <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="User Load">
      <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
      <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
        <boolProp name="LoopController.continue_forever">false</boolProp>
        <stringProp name="LoopController.loops">10</stringProp>
      </elementProp>
      <stringProp name="ThreadGroup.num_threads">50</stringProp>
      <stringProp name="ThreadGroup.ramp_time">30</stringProp>
    </ThreadGroup>

    <!-- HTTP Request -->
    <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Login Request">
      <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="" elementType="HTTPArgument">
            <boolProp name="HTTPArgument.always_encode">false</boolProp>
            <stringProp name="Argument.value">{"username":"admin","password":"password"}</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
      <stringProp name="HTTPSampler.domain">localhost</stringProp>
      <stringProp name="HTTPSampler.port">8080</stringProp>
      <stringProp name="HTTPSampler.path">/auth/login</stringProp>
      <stringProp name="HTTPSampler.method">POST</stringProp>
      <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
    </HTTPSamplerProxy>
  </hashTree>
</jmeterTestPlan>
```

## 📚 テストのベストプラクティス

### 1. テスト命名規則

```java
// 良い例：メソッド名でテスト内容が分かる
@Test
void testCreateUser_WhenUsernameAlreadyExists_ShouldThrowBusinessException()

@Test
void testAuthenticate_WhenValidCredentials_ShouldReturnUser()

@Test
void testFindById_WhenUserNotFound_ShouldReturnEmpty()

// 悪い例：何をテストしているか不明
@Test
void test1()

@Test
void testUser()
```

### 2. AAA パターン

```java
@Test
void testCreateUser_Success() {
    // Arrange (Given)
    CreateUserRequest request = new CreateUserRequest();
    request.username = "testuser";
    // ... 設定

    // Act (When)
    User result = userService.create(request, null);

    // Assert (Then)
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
}
```

### 3. テストの独立性

```java
@TestMethodOrder(OrderAnnotation.class)
class UserServiceTest {

    @Test
    @Order(1)
    void testCreateUser() {
        // 他のテストに依存しない独立したテスト
    }

    @Test
    @Order(2)
    void testUpdateUser() {
        // 他のテストの結果に依存しない
    }
}
```

---

このガイドを参考に、品質の高いテストコードを作成してください。
