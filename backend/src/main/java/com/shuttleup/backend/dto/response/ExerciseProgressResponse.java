package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

/** 種目の1日ごとの最大重量。 */
@Getter
@Builder
public class ExerciseProgressResponse {
    private LocalDate date;
    private Double maxWeightKg;
}
