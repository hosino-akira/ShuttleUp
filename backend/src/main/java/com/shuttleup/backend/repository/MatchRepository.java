package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTrainingSessionIdOrderByIdAsc(Long trainingSessionId);

    boolean existsByOpponentId(Long opponentId);

    /** Dashboardの勝敗集計に必要な得点だけを取得する。 */
    @Query("""
            select m.myScore as myScore, m.opponentScore as opponentScore
            from Match m
            where m.trainingSession.user.id = :userId
              and (:from is null or m.matchDate >= :from)
              and (:to is null or m.matchDate <= :to)
              and (:opponentId is null or m.opponent.id = :opponentId)
            """)
    List<MatchScoreRow> findDashboardScores(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("opponentId") Long opponentId);

    interface MatchScoreRow {
        Integer getMyScore();
        Integer getOpponentScore();
    }

    /** 分析用に対戦相手を一括取得し、N+1を防止する。 */
    @Query("""
            select m from Match m
            join fetch m.opponent
            where m.trainingSession.user.id = :userId
              and m.matchDate between :from and :to
            order by m.matchDate asc, m.id asc
            """)
    List<Match> findAnalysisMatches(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
