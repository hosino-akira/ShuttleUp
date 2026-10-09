package com.shuttleup.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * ユーザー基本情報を管理するエンティティ
 */
@Entity
@DynamicUpdate
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** ユーザーID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ユーザー名 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** メールアドレス */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** パスワードのハッシュ値。既存ユーザーは設定するまでログインできない。 */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** パスワード変更時に増やし、変更前の JWT を失効させる。 */
    @Column(name = "token_version", nullable = false)
    @ColumnDefault("0")
    private long tokenVersion;

    /** アカウントの状態。ログイン状態とは別に管理する。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** アバター画像のURL */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /** 作成日時 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
