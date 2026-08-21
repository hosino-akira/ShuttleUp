package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MatchResponse {

    private Long id;
    private Long trainingSessionId;
    private Long opponentId;
    private String opponentName;
    private LocalDate matchDate;
    private Integer myScore;
    private Integer opponentScore;
    private String videoUrl;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
