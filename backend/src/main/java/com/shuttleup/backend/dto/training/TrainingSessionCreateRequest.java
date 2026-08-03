package com.shuttleup.backend.dto.training;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingSessionCreateRequest {

    /** ユーザーID */
    private Long userId;

    /** トレーニング日 */
    private LocalDate trainingDate;

    /** トレーニング時間（分） */
    private Integer durationMinutes;

    /** トレーニング時の感覚 */
    private Integer feeling;

    /** メモ */
    private String note;
}