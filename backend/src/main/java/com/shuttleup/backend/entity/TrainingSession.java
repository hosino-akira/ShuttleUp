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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 1回のトレーニングを管理するエンティティ
 */
@Entity
@Table(name = "training_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {

    /** トレーニングID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** トレーニングを行ったユーザー */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** トレーニング日 */
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    /** トレーニング時間（分） */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** トレーニング時の感覚 */
    @Column(name = "feeling")
    private Integer feeling;

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
