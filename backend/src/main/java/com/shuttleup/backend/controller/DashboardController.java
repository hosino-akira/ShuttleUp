package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.response.DashboardResponse;
import com.shuttleup.backend.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Dashboard APIを提供するController。 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 指定ユーザーのDashboard集計を取得する。 */
    @GetMapping("/users/{userId}")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long exerciseId,
            @RequestParam(required = false) Long opponentId) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId, from, to, exerciseId, opponentId));
    }
}
