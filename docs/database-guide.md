# データベース層開発ガイド

このガイドでは、MyBatisを使用したデータアクセス層の実装について説明します。

## 🎯 データベース層の役割

- **データアクセス**: データベースとの CRUD 操作
- **SQLマッピング**: JavaオブジェクトとSQLの橋渡し
- **トランザクション境界**: データの整合性保証
- **パフォーマンス最適化**: 効率的なクエリ実行
- **データ変換**: データベース型とJava型の変換

## 📁 ファイル構成

```
src/main/java/com/example/
├── model/                    # データモデル（POJO）
│   ├── User.java
│   └── AuditLog.java
├── mapper/                   # MyBatisマッパーインターフェース
│   ├── UserMapper.java
│   └── AuditLogMapper.java
└── config/
    └── DatabaseInitializer.java

src/main/resources/
├── mapper/                   # XMLマッパーファイル（必要に応じて）
│   ├── UserMapper.xml
│   └── AuditLogMapper.xml
├── database-setup-h2.sql    # 開発用DDL
└── database-setup-postgres.sql # 本番用DDL
```

## 🏗️ モデルクラス設計

### 基本的なモデルクラス

```java
package com.example.model;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private Role role = Role.USER;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isActive = true;

    public enum Role {
        ADMIN, USER, SALES
    }

    // デフォルトコンストラクタ
    public User() {}

    // 便利コンストラクタ
    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // ... 他のgetter/setter

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
}
```

### 複雑なモデルクラス

```java
public class AuditLog {

    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private Status status = Status.SUCCESS;
    private String errorMessage;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        SUCCESS, FAILURE, ERROR
    }

    // コンストラクタ
    public AuditLog() {}

    public AuditLog(Long userId, String username, String action) {
        this.userId = userId;
        this.username = username;
        this.action = action;
    }

    public AuditLog(Long userId, String username, String action,
                   String resourceType, String resourceId) {
        this(userId, username, action);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    // Getter/Setter...
}
```

## 🗺️ マッパーインターフェース

### 基本的なCRUD操作

```java
package com.example.mapper;

import com.example.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * ユーザーを挿入
     */
    @Insert("INSERT INTO users (username, password, email, role, created_at, is_active) " +
            "VALUES (#{username}, #{password}, #{email}, #{role}, #{createdAt}, #{active})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * IDでユーザーを検索
     */
    @Select("SELECT id, username, password, email, role, created_at, is_active " +
            "FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "role", column = "role"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "active", column = "is_active")
    })
    Optional<User> findById(Long id);

    /**
     * ユーザー名でユーザーを検索
     */
    @Select("SELECT id, username, password, email, role, created_at, is_active " +
            "FROM users WHERE username = #{username}")
    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "role", column = "role"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "active", column = "is_active")
    })
    Optional<User> findByUsername(String username);

    /**
     * 全ユーザーを取得
     */
    @Select("SELECT id, username, password, email, role, created_at, is_active " +
            "FROM users ORDER BY created_at DESC")
    @ResultMap("userResultMap")
    List<User> findAll();

    /**
     * ユーザーを更新
     */
    @Update("UPDATE users SET username = #{username}, password = #{password}, " +
            "email = #{email}, role = #{role}, is_active = #{active} " +
            "WHERE id = #{id}")
    int update(User user);

    /**
     * ユーザーを削除
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);
}
```

### 複雑なクエリ

```java
@Mapper
public interface UserMapper {

    /**
     * 条件付き検索
     */
    @Select("<script>" +
            "SELECT id, username, password, email, role, created_at, is_active " +
            "FROM users " +
            "WHERE 1=1 " +
            "<if test='username != null'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='email != null'> AND email LIKE CONCAT('%', #{email}, '%') </if>" +
            "<if test='role != null'> AND role = #{role} </if>" +
            "<if test='isActive != null'> AND is_active = #{isActive} </if>" +
            "<if test='createdAfter != null'> AND created_at >= #{createdAfter} </if>" +
            "<if test='createdBefore != null'> AND created_at <= #{createdBefore} </if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"username\"'>username</when>" +
            "<when test='sortBy == \"email\"'>email</when>" +
            "<when test='sortBy == \"role\"'>role</when>" +
            "<otherwise>created_at</otherwise>" +
            "</choose> " +
            "<if test='sortDirection == \"ASC\"'>ASC</if>" +
            "<if test='sortDirection != \"ASC\"'>DESC</if> " +
            "LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    @ResultMap("userResultMap")
    List<User> search(@Param("username") String username,
                     @Param("email") String email,
                     @Param("role") User.Role role,
                     @Param("isActive") Boolean isActive,
                     @Param("createdAfter") LocalDateTime createdAfter,
                     @Param("createdBefore") LocalDateTime createdBefore,
                     @Param("sortBy") String sortBy,
                     @Param("sortDirection") String sortDirection,
                     @Param("size") int size,
                     @Param("offset") int offset);

    /**
     * 検索結果件数取得
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM users " +
            "WHERE 1=1 " +
            "<if test='username != null'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='email != null'> AND email LIKE CONCAT('%', #{email}, '%') </if>" +
            "<if test='role != null'> AND role = #{role} </if>" +
            "<if test='isActive != null'> AND is_active = #{isActive} </if>" +
            "<if test='createdAfter != null'> AND created_at >= #{createdAfter} </if>" +
            "<if test='createdBefore != null'> AND created_at <= #{createdBefore} </if>" +
            "</script>")
    long countByCriteria(@Param("username") String username,
                        @Param("email") String email,
                        @Param("role") User.Role role,
                        @Param("isActive") Boolean isActive,
                        @Param("createdAfter") LocalDateTime createdAfter,
                        @Param("createdBefore") LocalDateTime createdBefore);

    /**
     * バッチ挿入
     */
    @Insert("<script>" +
            "INSERT INTO users (username, password, email, role, created_at, is_active) VALUES " +
            "<foreach collection='users' item='user' separator=','>" +
            "(#{user.username}, #{user.password}, #{user.email}, #{user.role}, #{user.createdAt}, #{user.active})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("users") List<User> users);

    /**
     * 統計情報取得
     */
    @Select("SELECT role, COUNT(*) as count FROM users WHERE is_active = true GROUP BY role")
    @Results({
        @Result(property = "role", column = "role"),
        @Result(property = "count", column = "count")
    })
    List<RoleCount> getUserCountByRole();
}
```

## 📄 XMLマッパー（複雑なクエリ用）

### UserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.UserMapper">

    <!-- 結果マッピング定義 -->
    <resultMap id="userResultMap" type="com.example.model.User">
        <id property="id" column="id"/>
        <result property="username" column="username"/>
        <result property="password" column="password"/>
        <result property="email" column="email"/>
        <result property="role" column="role"/>
        <result property="createdAt" column="created_at"/>
        <result property="active" column="is_active"/>
    </resultMap>

    <!-- 共通SQL片 -->
    <sql id="userColumns">
        id, username, password, email, role, created_at, is_active
    </sql>

    <sql id="userTable">
        users
    </sql>

    <!-- 複雑な検索クエリ -->
    <select id="searchUsers" resultMap="userResultMap">
        SELECT <include refid="userColumns"/>
        FROM <include refid="userTable"/>
        <where>
            <if test="criteria.username != null and criteria.username != ''">
                AND username LIKE CONCAT('%', #{criteria.username}, '%')
            </if>
            <if test="criteria.email != null and criteria.email != ''">
                AND email LIKE CONCAT('%', #{criteria.email}, '%')
            </if>
            <if test="criteria.role != null">
                AND role = #{criteria.role}
            </if>
            <if test="criteria.isActive != null">
                AND is_active = #{criteria.isActive}
            </if>
            <if test="criteria.createdAfter != null">
                AND created_at >= #{criteria.createdAfter}
            </if>
            <if test="criteria.createdBefore != null">
                AND created_at &lt;= #{criteria.createdBefore}
            </if>
        </where>
        ORDER BY
        <choose>
            <when test="criteria.sortBy == 'username'">username</when>
            <when test="criteria.sortBy == 'email'">email</when>
            <when test="criteria.sortBy == 'role'">role</when>
            <otherwise>created_at</otherwise>
        </choose>
        <if test="criteria.sortDirection == 'ASC'">ASC</if>
        <if test="criteria.sortDirection != 'ASC'">DESC</if>
        LIMIT #{criteria.size} OFFSET #{criteria.offset}
    </select>

    <!-- 動的更新 -->
    <update id="updateSelective">
        UPDATE <include refid="userTable"/>
        <set>
            <if test="username != null">username = #{username},</if>
            <if test="password != null">password = #{password},</if>
            <if test="email != null">email = #{email},</if>
            <if test="role != null">role = #{role},</if>
            <if test="active != null">is_active = #{active},</if>
        </set>
        WHERE id = #{id}
    </update>

    <!-- 複雑な集計クエリ -->
    <select id="getUserStatistics" resultType="map">
        SELECT
            COUNT(*) as totalUsers,
            COUNT(CASE WHEN is_active = true THEN 1 END) as activeUsers,
            COUNT(CASE WHEN is_active = false THEN 1 END) as inactiveUsers,
            COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as adminUsers,
            COUNT(CASE WHEN role = 'USER' THEN 1 END) as regularUsers,
            COUNT(CASE WHEN role = 'SALES' THEN 1 END) as salesUsers,
            COUNT(CASE WHEN created_at >= #{startDate} THEN 1 END) as newUsers
        FROM <include refid="userTable"/>
    </select>
</mapper>
```

## 🗄️ データベース設計

### テーブル定義（H2/開発用）

```sql
-- database-setup-h2.sql

-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'USER', 'SALES'))
);

-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('SUCCESS', 'FAILURE', 'ERROR')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

-- 初期データ投入
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'admin@example.com', 'ADMIN'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'user1@example.com', 'USER'),
('sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJfbPpWO.1fFnKm', 'sales1@example.com', 'SALES');
```

### PostgreSQL用定義

```sql
-- database-setup-postgres.sql

-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'USER', 'SALES'))
);

-- 更新時刻自動更新のトリガー
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details TEXT,
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('SUCCESS', 'FAILURE', 'ERROR'))
);

-- インデックス作成
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
```

## ⚙️ MyBatis設定

### application.yaml設定

```yaml
# 開発環境
quarkus:
  mybatis:
    mapper-locations: classpath*:mapper/*.xml
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
      cache-enabled: true
      lazy-loading-enabled: true
      multiple-result-sets-enabled: true
      use-column-label: true
      use-generated-keys: true
      auto-mapping-behavior: partial
      default-executor-type: simple
      default-statement-timeout: 25
      default-fetch-size: 100
```

## 🧪 テスト実装

### マッパーテスト

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
    }

    @Test
    void testSearch() {
        // Given
        insertTestUsers();

        // When
        List<User> results = userMapper.search(
            "test", null, User.Role.USER, true,
            null, null, "username", "ASC", 10, 0
        );

        // Then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(u -> u.getUsername().contains("test")));
    }

    private void insertTestUsers() {
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setUsername("testuser" + i);
            user.setPassword("password" + i);
            user.setEmail("test" + i + "@example.com");
            user.setRole(User.Role.USER);
            userMapper.insert(user);
        }
    }
}
```

---

このガイドを参考に、効率的で保守性の高いデータアクセス層を実装してください。
