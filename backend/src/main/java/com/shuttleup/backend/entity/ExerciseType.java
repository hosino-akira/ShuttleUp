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
 * トレーニングの中分類を管理するエンティティ
 */
@Entity
@Table(name = "exercise_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseType {

    /** 種別ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属するカテゴリ */
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ExerciseCategory category;

    /** 種別名 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 表示順 */
    @Column(name = "display_order")
    private Integer displayOrder;

    /** 有効フラグ */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /** 作成日時 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
