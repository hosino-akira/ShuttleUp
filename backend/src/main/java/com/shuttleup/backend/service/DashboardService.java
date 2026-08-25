package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.response.DashboardResponse;
import com.shuttleup.backend.dto.response.DashboardSummaryResponse;
import com.shuttleup.backend.dto.response.ExerciseProgressResponse;
import com.shuttleup.backend.dto.response.MatchResultSummaryResponse;
import com.shuttleup.backend.dto.response.MonthlyTrainingResponse;
import com.shuttleup.backend.dto.response.OpponentMatchSummaryResponse;
import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.Match;
import com.shuttleup.backend.entity.Opponent;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dashboardの集計を行うサービス。 */
@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final MatchRepository matchRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final ExerciseRepository exerciseRepository;
    private final OpponentRepository opponentRepository;

    public DashboardService(UserRepository userRepository,
            TrainingSessionRepository trainingSessionRepository,
            MatchRepository matchRepository,
            TrainingRecordRepository trainingRecordRepository,
            ExerciseRepository exerciseRepository,
            OpponentRepository opponentRepository) {
        this.userRepository = userRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.matchRepository = matchRepository;
        this.trainingRecordRepository = trainingRecordRepository;
        this.exerciseRepository = exerciseRepository;
        this.opponentRepository = opponentRepository;
    }

    /** 条件に合うDashboard情報を返す。 */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId, LocalDate from, LocalDate to,
            Long exerciseId, Long opponentId) {
        validatePeriod(from, to);
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("指定されたユーザーが見つかりません。");
        }

        Exercise exercise = exerciseId == null ? null : findAvailableExercise(userId, exerciseId);
        Opponent opponent = opponentId == null ? null : findOwnedOpponent(userId, opponentId);
        List<TrainingSessionRepository.TrainingSummaryRow> trainingRows =
                trainingSessionRepository.findDashboardRows(userId, from, to);
        List<MatchRepository.MatchScoreRow> matchRows =
                matchRepository.findDashboardScores(userId, from, to, null);

        long totalMinutes = trainingRows.stream()
                .mapToLong(row -> row.getDurationMinutes() == null ? 0 : row.getDurationMinutes()).sum();
        long wins = matchRows.stream()
                .filter(row -> Match.isWin(row.getMyScore(), row.getOpponentScore())).count();
        long losses = matchRows.stream()
                .filter(row -> Match.isLoss(row.getMyScore(), row.getOpponentScore())).count();
        long matchCount = matchRows.size();

        return DashboardResponse.builder()
                .summary(DashboardSummaryResponse.builder()
                        .trainingSessionCount(trainingRows.size())
                        .totalDurationMinutes(totalMinutes)
                        .matchCount(matchCount).winCount(wins).lossCount(losses)
                        .winRate(calculateWinRate(wins, matchCount)).build())
                .monthlyTraining(createMonthlyTraining(trainingRows))
                .matchResults(MatchResultSummaryResponse.builder().wins(wins).losses(losses).build())
                .opponentMatchSummary(opponent == null ? null
                        : createOpponentSummary(userId, from, to, opponent))
                .exerciseProgress(exercise == null ? List.of()
                        : createExerciseProgress(userId, exercise.getId(), from, to))
                .build();
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("開始日は終了日以前の日付を指定してください。");
        }
    }

    private Exercise findAvailableExercise(Long userId, Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("指定された種目が見つかりません。"));
        boolean available = Boolean.TRUE.equals(exercise.getSystemPreset())
                || exercise.getUser() != null && userId.equals(exercise.getUser().getId());
        if (!available) {
            throw new IllegalArgumentException("指定された種目が見つかりません。");
        }
        return exercise;
    }

    private Opponent findOwnedOpponent(Long userId, Long opponentId) {
        Opponent opponent = opponentRepository.findById(opponentId)
                .orElseThrow(() -> new IllegalArgumentException("指定された対戦相手が見つかりません。"));
        if (!userId.equals(opponent.getUser().getId())) {
            throw new IllegalArgumentException("指定された対戦相手が見つかりません。");
        }
        return opponent;
    }

    private List<MonthlyTrainingResponse> createMonthlyTraining(
            List<TrainingSessionRepository.TrainingSummaryRow> rows) {
        if (rows.isEmpty()) return List.of();
        Map<YearMonth, long[]> totals = new LinkedHashMap<>();
        for (var row : rows) {
            YearMonth month = YearMonth.from(row.getTrainingDate());
            long[] value = totals.computeIfAbsent(month, ignored -> new long[2]);
            value[0]++;
            value[1] += row.getDurationMinutes() == null ? 0 : row.getDurationMinutes();
        }
        YearMonth first = YearMonth.from(rows.getFirst().getTrainingDate());
        YearMonth last = YearMonth.from(rows.getLast().getTrainingDate());
        List<MonthlyTrainingResponse> result = new ArrayList<>();
        for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
            long[] value = totals.getOrDefault(month, new long[2]);
            result.add(MonthlyTrainingResponse.builder().month(month.toString())
                    .sessionCount(value[0]).durationMinutes(value[1]).build());
        }
        return result;
    }

    private OpponentMatchSummaryResponse createOpponentSummary(Long userId, LocalDate from,
            LocalDate to, Opponent opponent) {
        List<MatchRepository.MatchScoreRow> rows = matchRepository.findDashboardScores(
                userId, from, to, opponent.getId());
        long wins = rows.stream().filter(r -> Match.isWin(r.getMyScore(), r.getOpponentScore())).count();
        long losses = rows.stream().filter(r -> Match.isLoss(r.getMyScore(), r.getOpponentScore())).count();
        return OpponentMatchSummaryResponse.builder().opponentId(opponent.getId())
                .opponentName(opponent.getName()).matchCount(rows.size()).winCount(wins)
                .lossCount(losses).winRate(calculateWinRate(wins, rows.size())).build();
    }

    private List<ExerciseProgressResponse> createExerciseProgress(Long userId, Long exerciseId,
            LocalDate from, LocalDate to) {
        return trainingRecordRepository.findExerciseProgress(userId, exerciseId, from, to).stream()
                .map(row -> ExerciseProgressResponse.builder().date(row.getTrainingDate())
                        .maxWeightKg(row.getMaxWeightKg()).build()).toList();
    }

    static double calculateWinRate(long wins, long matches) {
        return matches == 0 ? 0.0 : Math.round(wins * 1000.0 / matches) / 10.0;
    }
}
