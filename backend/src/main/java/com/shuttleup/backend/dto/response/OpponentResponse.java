package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OpponentResponse {

    private Long id;
    private Long userId;
    private String name;
    private Boolean enabled;
    private Boolean available;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
