package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.response.DashboardResponse;
import com.shuttleup.backend.dto.response.DashboardSummaryResponse;
import com.shuttleup.backend.dto.response.MatchResultSummaryResponse;
import com.shuttleup.backend.service.DashboardService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardControllerTest {
    @Test
    void 正常なリクエストはHTTP200とDTO構造を返す() {
        DashboardService service = mock(DashboardService.class);
        DashboardResponse dashboard = DashboardResponse.builder()
                .summary(DashboardSummaryResponse.builder().build())
                .monthlyTraining(List.of())
                .matchResults(MatchResultSummaryResponse.builder().build())
                .opponentMatchSummary(null).exerciseProgress(List.of()).build();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(service.getDashboard(1L, from, to, 2L, 3L)).thenReturn(dashboard);

        var response = new DashboardController(service)
                .getDashboard(1L, from, to, 2L, 3L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(dashboard);
        assertThat(response.getBody().getSummary()).isNotNull();
        assertThat(response.getBody().getMatchResults()).isNotNull();
        verify(service).getDashboard(1L, from, to, 2L, 3L);
    }
}
