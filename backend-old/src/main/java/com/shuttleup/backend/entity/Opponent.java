package com.shuttleup.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 対戦相手を管理するエンティティ
 */
@Entity
@Table(name = "opponents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opponent {

    /** 対戦相手ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 対戦相手を登録したユーザー */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 対戦相手名 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** メモ */
    @Column(name = "memo", length = 1000)
    private String memo;

    /** 作成日時 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
