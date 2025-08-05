# サービス層開発ガイド

このガイドでは、ビジネスロジックを実装するサービス層の開発について説明します。

## 🎯 サービス層の役割

- **ビジネスロジック実装**: アプリケーションの核となる処理
- **トランザクション管理**: データの整合性を保証
- **データ変換**: DTOとモデル間の変換
- **外部サービス連携**: 他のシステムとの連携
- **キャッシュ管理**: パフォーマンス向上のためのデータキャッシュ

## 📁 ファイル構成

```
src/main/java/com/example/service/
├── UserService.java           # ユーザー関連ビジネスロジック
├── AuthService.java           # 認証関連ビジネスロジック
├── AuditLogService.java       # 監査ログサービス
├── MessageService.java        # 国際化メッセージサービス
├── JwtService.java           # JWT関連サービス
└── EmailService.java         # メール送信サービス
```

## 🔧 基本実装

### サービスクラスの基本構造

```java
package com.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    UserMapper userMapper;

    @Inject
    MessageService messageService;

    @Inject
    AuditLogService auditLogService;

    // メソッド実装...
}
```

### CRUD操作の実装

```java
/**
 * ユーザー作成
 */
@Transactional
public User create(CreateUserRequest request, HttpHeaders headers) {
    LOG.infof("ユーザー作成開始: username=%s", request.username);

    // 重複チェック
    Optional<User> existingUser = userMapper.findByUsername(request.username);
    if (existingUser.isPresent()) {
        String message = messageService.getMessage("error.user.already.exists", headers);
        throw new BusinessException("USER_ALREADY_EXISTS", message);
    }

    // ユーザー作成
    User user = new User();
    user.setUsername(request.username);
    user.setPassword(BcryptUtil.bcryptHash(request.password));
    user.setEmail(request.email);
    user.setRole(request.role);

    userMapper.insert(user);

    // 監査ログ記録
    auditLogService.logSuccess(user.getId(), user.getUsername(),
        "USER_CREATE", "User", user.getId().toString());

    LOG.infof("ユーザー作成完了: id=%d, username=%s", user.getId(), user.getUsername());
    return user;
}

/**
 * ユーザー検索
 */
public Optional<User> findById(Long id) {
    LOG.debugf("ユーザー検索: id=%d", id);
    return userMapper.findById(id);
}

/**
 * ユーザー一覧取得
 */
public List<User> findAll() {
    LOG.debug("ユーザー一覧取得");
    return userMapper.findAll();
}

/**
 * ユーザー更新
 */
@Transactional
public User update(Long id, UpdateUserRequest request, HttpHeaders headers) {
    LOG.infof("ユーザー更新開始: id=%d", id);

    // 存在チェック
    User user = userMapper.findById(id)
        .orElseThrow(() -> {
            String message = messageService.getMessage("error.user.not.found", headers);
            return new NotFoundException("USER_NOT_FOUND", message);
        });

    // 更新処理
    user.setEmail(request.email);
    user.setRole(request.role);

    if (request.password != null && !request.password.isEmpty()) {
        user.setPassword(BcryptUtil.bcryptHash(request.password));
    }

    userMapper.update(user);

    // 監査ログ記録
    auditLogService.logSuccess(user.getId(), user.getUsername(),
        "USER_UPDATE", "User", user.getId().toString());

    LOG.infof("ユーザー更新完了: id=%d", id);
    return user;
}

/**
 * ユーザー削除
 */
@Transactional
public void delete(Long id) {
    LOG.infof("ユーザー削除開始: id=%d", id);

    User user = userMapper.findById(id)
        .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "ユーザーが見つかりません"));

    userMapper.deleteById(id);

    // 監査ログ記録
    auditLogService.logSuccess(user.getId(), user.getUsername(),
        "USER_DELETE", "User", user.getId().toString());

    LOG.infof("ユーザー削除完了: id=%d", id);
}
```

### 複雑なビジネスロジック

```java
/**
 * ユーザー認証
 */
public Optional<User> authenticate(String username, String password) {
    LOG.infof("認証試行: username=%s", username);

    Optional<User> userOpt = userMapper.findActiveByUsername(username);
    if (userOpt.isPresent()) {
        User user = userOpt.get();

        if (BcryptUtil.matches(password, user.getPassword())) {
            // 認証成功
            auditLogService.logSuccess(user.getId(), user.getUsername(),
                "USER_LOGIN", "User", user.getId().toString());

            LOG.infof("認証成功: username=%s", username);
            return Optional.of(user);
        } else {
            // パスワード不一致
            auditLogService.logFailure(null, username, "USER_LOGIN", "User", null,
                "パスワードが一致しません");
        }
    } else {
        // ユーザーが存在しない
        auditLogService.logFailure(null, username, "USER_LOGIN", "User", null,
            "ユーザーが存在しません");
    }

    LOG.warnf("認証失敗: username=%s", username);
    return Optional.empty();
}

/**
 * パスワードリセット
 */
@Transactional
public void resetPassword(String email, HttpHeaders headers) {
    LOG.infof("パスワードリセット開始: email=%s", email);

    // ユーザー検索
    Optional<User> userOpt = userMapper.findByEmail(email);
    if (userOpt.isEmpty()) {
        String message = messageService.getMessage("error.user.not.found", headers);
        throw new NotFoundException("USER_NOT_FOUND", message);
    }

    User user = userOpt.get();

    // 一時パスワード生成
    String temporaryPassword = generateTemporaryPassword();
    user.setPassword(BcryptUtil.bcryptHash(temporaryPassword));
    user.setPasswordResetRequired(true);

    userMapper.update(user);

    // メール送信
    emailService.sendPasswordResetEmail(user.getEmail(), temporaryPassword);

    // 監査ログ記録
    auditLogService.logSuccess(user.getId(), user.getUsername(),
        "PASSWORD_RESET", "User", user.getId().toString());

    LOG.infof("パスワードリセット完了: email=%s", email);
}

/**
 * ユーザー統計情報取得
 */
public UserStatistics getUserStatistics() {
    LOG.debug("ユーザー統計情報取得");

    long totalUsers = userMapper.count();
    long activeUsers = userMapper.countActive();
    long inactiveUsers = totalUsers - activeUsers;

    Map<User.Role, Long> usersByRole = Arrays.stream(User.Role.values())
        .collect(Collectors.toMap(
            role -> role,
            role -> userMapper.countByRole(role)
        ));

    return new UserStatistics(totalUsers, activeUsers, inactiveUsers, usersByRole);
}
```

## 🔄 トランザクション管理

### 基本的なトランザクション

```java
@Transactional
public User createUserWithProfile(CreateUserRequest request) {
    // ユーザー作成
    User user = new User();
    // ... 設定
    userMapper.insert(user);

    // プロフィール作成
    UserProfile profile = new UserProfile();
    profile.setUserId(user.getId());
    // ... 設定
    userProfileMapper.insert(profile);

    return user;
}
```

### トランザクション属性の指定

```java
// 新しいトランザクションを開始
@Transactional(Transactional.TxType.REQUIRES_NEW)
public void logAuditInNewTransaction(String action) {
    // 監査ログは独立したトランザクションで記録
    auditLogService.log(action);
}

// 読み取り専用トランザクション
@Transactional(Transactional.TxType.SUPPORTS)
public List<User> findUsers() {
    return userMapper.findAll();
}

// トランザクション不要
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public void sendNotification(String message) {
    // 外部サービス呼び出し
    notificationService.send(message);
}
```

### 例外処理とロールバック

```java
@Transactional
public void transferUserData(Long fromUserId, Long toUserId) {
    try {
        // データ移行処理
        User fromUser = userMapper.findById(fromUserId)
            .orElseThrow(() -> new NotFoundException("移行元ユーザーが見つかりません"));

        User toUser = userMapper.findById(toUserId)
            .orElseThrow(() -> new NotFoundException("移行先ユーザーが見つかりません"));

        // データ移行
        dataTransferService.transfer(fromUser, toUser);

        // 移行元ユーザーを無効化
        fromUser.setActive(false);
        userMapper.update(fromUser);

    } catch (Exception e) {
        LOG.error("データ移行エラー", e);
        // RuntimeExceptionの場合、自動的にロールバックされる
        throw new BusinessException("DATA_TRANSFER_FAILED", "データ移行に失敗しました", e);
    }
}
```

## 🔧 依存性注入とサービス連携

### サービス間の連携

```java
@ApplicationScoped
public class UserService {

    @Inject
    UserMapper userMapper;

    @Inject
    EmailService emailService;

    @Inject
    AuditLogService auditLogService;

    @Inject
    CacheService cacheService;

    @Transactional
    public User createUser(CreateUserRequest request) {
        // ユーザー作成
        User user = new User();
        // ... 設定
        userMapper.insert(user);

        // ウェルカムメール送信
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        // キャッシュ更新
        cacheService.evictUserCache();

        // 監査ログ記録
        auditLogService.logSuccess(user.getId(), user.getUsername(),
            "USER_CREATE", "User", user.getId().toString());

        return user;
    }
}
```

### 設定値の注入

```java
@ApplicationScoped
public class UserService {

    @ConfigProperty(name = "app.user.max-login-attempts", defaultValue = "5")
    int maxLoginAttempts;

    @ConfigProperty(name = "app.user.password-expiry-days", defaultValue = "90")
    int passwordExpiryDays;

    public boolean isPasswordExpired(User user) {
        LocalDateTime expiryDate = user.getPasswordChangedAt()
            .plusDays(passwordExpiryDays);
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
```

## 📊 キャッシュ実装

### 基本的なキャッシュ

```java
@ApplicationScoped
public class UserService {

    private final Map<Long, User> userCache = new ConcurrentHashMap<>();

    public User findById(Long id) {
        // キャッシュから取得を試行
        User cachedUser = userCache.get(id);
        if (cachedUser != null) {
            LOG.debugf("キャッシュヒット: userId=%d", id);
            return cachedUser;
        }

        // データベースから取得
        Optional<User> userOpt = userMapper.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userCache.put(id, user);
            LOG.debugf("キャッシュ更新: userId=%d", id);
            return user;
        }

        return null;
    }

    @Transactional
    public User update(Long id, UpdateUserRequest request) {
        User user = update(id, request);

        // キャッシュ更新
        userCache.put(id, user);

        return user;
    }

    public void evictCache(Long id) {
        userCache.remove(id);
        LOG.debugf("キャッシュ削除: userId=%d", id);
    }
}
```

## 🔍 検索とページング

### 検索条件の実装

```java
public class UserSearchCriteria {
    private String username;
    private String email;
    private User.Role role;
    private Boolean isActive;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // getter/setter...
}

@ApplicationScoped
public class UserService {

    public PagedResult<User> search(UserSearchCriteria criteria) {
        LOG.debugf("ユーザー検索: criteria=%s", criteria);

        // 検索実行
        List<User> users = userMapper.search(criteria);
        long totalCount = userMapper.countByCriteria(criteria);

        // ページング情報計算
        int totalPages = (int) Math.ceil((double) totalCount / criteria.getSize());
        boolean hasNext = criteria.getPage() < totalPages - 1;
        boolean hasPrevious = criteria.getPage() > 0;

        return new PagedResult<>(
            users,
            criteria.getPage(),
            criteria.getSize(),
            totalCount,
            totalPages,
            hasNext,
            hasPrevious
        );
    }
}
```

## 🧪 テスト実装

### 単体テスト

```java
@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserMapper userMapper;

    @InjectMock
    MessageService messageService;

    @Test
    void testCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.username = "testuser";
        request.password = "TestPass123";
        request.email = "test@example.com";
        request.role = User.Role.USER;

        when(userMapper.findByUsername("testuser"))
            .thenReturn(Optional.empty());

        // When
        User result = userService.create(request, null);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testCreateUserDuplicate() {
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
    }
}
```

## 📚 ベストプラクティス

### 1. 単一責任の原則

```java
// 良い例：各サービスが明確な責任を持つ
@ApplicationScoped
public class UserService {
    // ユーザー関連のビジネスロジックのみ
}

@ApplicationScoped
public class AuthService {
    // 認証関連のビジネスロジックのみ
}

@ApplicationScoped
public class EmailService {
    // メール送信関連のロジックのみ
}
```

### 2. 例外処理

```java
// ビジネス例外の適切な使用
public User findById(Long id) {
    return userMapper.findById(id)
        .orElseThrow(() -> new NotFoundException(
            "USER_NOT_FOUND",
            "ユーザーが見つかりません: id=" + id
        ));
}

// 技術的例外の適切な処理
@Transactional
public void processUsers() {
    try {
        // 処理...
    } catch (SQLException e) {
        LOG.error("データベースエラー", e);
        throw new SystemException("DATABASE_ERROR", "データベース処理でエラーが発生しました", e);
    }
}
```

### 3. ログ出力

```java
public User createUser(CreateUserRequest request) {
    LOG.infof("ユーザー作成開始: username=%s", request.username);

    try {
        // 処理...
        LOG.infof("ユーザー作成完了: id=%d, username=%s", user.getId(), user.getUsername());
        return user;
    } catch (Exception e) {
        LOG.errorf(e, "ユーザー作成エラー: username=%s", request.username);
        throw e;
    }
}
```

---

このガイドを参考に、保守性が高く、テストしやすいサービス層を実装してください。
