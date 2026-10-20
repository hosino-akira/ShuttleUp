package com.shuttleup.backend.service;

import com.shuttleup.backend.security.CurrentUser;

import com.shuttleup.backend.dto.response.AnalysisDataResponse;
import com.shuttleup.backend.dto.response.AnalysisTrainingRecordResponse;
import com.shuttleup.backend.dto.response.MatchResponse;
import com.shuttleup.backend.dto.response.TrainingSessionResponse;
import com.shuttleup.backend.entity.Match;
import com.shuttleup.backend.entity.TrainingRecord;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Python分析サービス向けデータを取得する。 */
@Service
public class AnalysisDataService {
    private final UserRepository userRepository;
    private final TrainingSessionRepository sessionRepository;
    private final TrainingRecordRepository recordRepository;
    private final MatchRepository matchRepository;
    private final ExerciseCatalogService catalogs;

    public AnalysisDataService(UserRepository userRepository,
            TrainingSessionRepository sessionRepository,
            TrainingRecordRepository recordRepository,
            MatchRepository matchRepository, ExerciseCatalogService catalogs) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.matchRepository = matchRepository;
        this.catalogs = catalogs;
    }

    /** 指定期間の分析用データを返す。 */
    @Transactional(readOnly = true)
    public AnalysisDataResponse getAnalysisData(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("開始日は終了日以前の日付を指定してください。");
        }
        CurrentUser.requireOwner(userId);
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("指定されたユーザーが見つかりません。");
        }
        var catalog = catalogs.load();
        return AnalysisDataResponse.builder()
                .userId(userId)
                .sessions(sessionRepository
                        .findByUserIdAndTrainingDateBetweenOrderByTrainingDateAscIdAsc(userId, from, to)
                        .stream().map(this::toSessionResponse).toList())
                .records(recordRepository.findAnalysisRecords(userId, from, to)
                        .stream().map(record -> toRecordResponse(record, catalog)).toList())
                .matches(matchRepository.findAnalysisMatches(userId, from, to)
                        .stream().map(this::toMatchResponse).toList())
                .build();
    }

    private TrainingSessionResponse toSessionResponse(TrainingSession session) {
        return TrainingSessionResponse.builder().id(session.getId()).userId(session.getUser().getId())
                .trainingDate(session.getTrainingDate()).durationMinutes(session.getDurationMinutes())
                .feeling(session.getFeeling()).note(session.getNote()).createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt()).build();
    }

    private AnalysisTrainingRecordResponse toRecordResponse(TrainingRecord record, ExerciseCatalogService.Catalog catalog) {
        return AnalysisTrainingRecordResponse.builder().id(record.getId())
                .trainingSessionId(record.getTrainingSession().getId())
                .exerciseId(record.getExercise().getId()).exerciseName(catalog.exerciseName(record.getExercise()))
                .categoryName(catalog.categoryName(record.getExercise().getExerciseType().getCategory()))
                .statisticsType(record.getExercise().getExerciseType().getCategory().getStatisticsType())
                .sets(record.getSets()).repetitions(record.getRepetitions()).weightKg(record.getWeightKg())
                .durationMinutes(record.getDurationMinutes()).distanceMeters(record.getDistanceMeters())
                .successCount(record.getSuccessCount()).attemptCount(record.getAttemptCount())
                .note(record.getNote()).build();
    }

    private MatchResponse toMatchResponse(Match match) {
        return MatchResponse.builder().id(match.getId()).trainingSessionId(match.getTrainingSession().getId())
                .opponentId(match.getOpponent().getId()).opponentName(match.getOpponent().getName())
                .matchDate(match.getTrainingSession().getTrainingDate()).myScore(match.getMyScore())
                .opponentScore(match.getOpponentScore()).videoUrl(match.getVideoUrl()).note(match.getNote())
                .createdAt(match.getCreatedAt()).updatedAt(match.getUpdatedAt()).build();
    }
}
