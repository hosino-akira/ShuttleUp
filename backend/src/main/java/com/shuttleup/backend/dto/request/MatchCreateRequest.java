package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MatchCreateRequest {

    @NotNull
    private Long opponentId;

    @NotNull
    private LocalDate matchDate;

    @Min(0)
    private Integer myScore;

    @Min(0)
    private Integer opponentScore;

    @Size(max = 2048)
    private String videoUrl;

    @Size(max = 1000)
    private String note;
}
