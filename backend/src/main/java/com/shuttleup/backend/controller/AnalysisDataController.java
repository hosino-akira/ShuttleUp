package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.response.AnalysisDataResponse;
import com.shuttleup.backend.service.AnalysisDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Python分析サービス向けの読み取りAPI。 */
@RestController
@RequestMapping("/api/analysis-data")
public class AnalysisDataController {
    private final AnalysisDataService analysisDataService;

    public AnalysisDataController(AnalysisDataService analysisDataService) {
        this.analysisDataService = analysisDataService;
    }

    /** 指定ユーザー・期間の分析用データを一括取得する。 */
    @GetMapping("/users/{userId}")
    public ResponseEntity<AnalysisDataResponse> getAnalysisData(
            @PathVariable Long userId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analysisDataService.getAnalysisData(userId, from, to));
    }
}
