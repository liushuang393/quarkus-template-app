package com.example.mapper;

import com.example.model.User;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.*;

/** ユーザーマッパー（MyBatis） */
@Mapper
public interface UserMapper {

  /** ユーザーを挿入 */
  @Insert(
      "INSERT INTO users (username, password, email, role, created_at, is_active) "
          + "VALUES (#{username}, #{password}, #{email}, #{role}, #{createdAt}, #{active})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(User user);

  /** IDでユーザーを検索 */
  @Select(
      "SELECT id, username, password, email, role, created_at, is_active "
          + "FROM users WHERE id = #{id}")
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

  /** ユーザー名でユーザーを検索 */
  @Select(
      "SELECT id, username, password, email, role, created_at, is_active "
          + "FROM users WHERE username = #{username}")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "username", column = "username"),
    @Result(property = "password", column = "password"),
    @Result(property = "email", column = "email"),
    @Result(property = "role", column = "role"),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "active", column = "is_active")
  })
  Optional<User> findByUsername(String username);

  /** アクティブなユーザーをユーザー名で検索 */
  @Select(
      "SELECT id, username, password, email, role, created_at, is_active "
          + "FROM users WHERE username = #{username} AND is_active = true")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "username", column = "username"),
    @Result(property = "password", column = "password"),
    @Result(property = "email", column = "email"),
    @Result(property = "role", column = "role"),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "active", column = "is_active")
  })
  Optional<User> findActiveByUsername(String username);

  /** 全ユーザーを取得 */
  @Select(
      "SELECT id, username, password, email, role, created_at, is_active "
          + "FROM users ORDER BY created_at DESC")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "username", column = "username"),
    @Result(property = "password", column = "password"),
    @Result(property = "email", column = "email"),
    @Result(property = "role", column = "role"),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "active", column = "is_active")
  })
  List<User> findAll();

  /** ユーザーを更新 */
  @Update(
      "UPDATE users SET username = #{username}, password = #{password}, "
          + "email = #{email}, role = #{role}, is_active = #{active} "
          + "WHERE id = #{id}")
  int update(User user);

  /** ユーザーを削除 */
  @Delete("DELETE FROM users WHERE id = #{id}")
  int deleteById(Long id);

  /** ユーザーを無効化 */
  @Update("UPDATE users SET is_active = false WHERE id = #{id}")
  int deactivateById(Long id);

  /** ユーザー数を取得 */
  @Select("SELECT COUNT(*) FROM users")
  long count();

  /** アクティブユーザー数を取得 */
  @Select("SELECT COUNT(*) FROM users WHERE is_active = true")
  long countActive();

  /** ロール別ユーザー数を取得 */
  @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
  long countByRole(@Param("role") User.Role role);
}
