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
 * 一回のトレーニング明細を管理するエンティティ
 */
@Entity
@Table(name = "training_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRecord {

    /** トレーニング明細ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属するトレーニング */
    @ManyToOne
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    /** 実施した種目 */
    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /** セット数 */
    @Column(name = "sets")
    private Integer sets;

    /** 反復回数 */
    @Column(name = "repetitions")
    private Integer repetitions;

    /** 負荷重量 */
    @Column(name = "weight_kg")
    private Double weightKg;

    /** 実施時間（分） */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /** 移動距離 */
    @Column(name = "distance_meters")
    private Double distanceMeters;

    /** 成功回数 */
    @Column(name = "success_count")
    private Integer successCount;

    /** 試行回数 */
    @Column(name = "attempt_count")
    private Integer attemptCount;

    /** メモ */
    @Column(name = "note", length = 1000)
    private String note;

    /** 作成日時 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
