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
 * 試合を管理するエンティティ
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    /** 試合ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属するトレーニング */
    @ManyToOne
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    /** 対戦相手 */
    @ManyToOne
    @JoinColumn(name = "opponent_id", nullable = false)
    private Opponent opponent;

    /** 試合日 */
    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    /** 自分の得点 */
    @Column(name = "my_score")
    private Integer myScore;

    /** 対戦相手の得点 */
    @Column(name = "opponent_score")
    private Integer opponentScore;

    /** 試合動画のURL */
    @Column(name = "video_url")
    private String videoUrl;

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
