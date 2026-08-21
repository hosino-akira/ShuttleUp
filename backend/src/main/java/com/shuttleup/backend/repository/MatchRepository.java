package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTrainingSessionIdOrderByIdAsc(Long trainingSessionId);

    boolean existsByOpponentId(Long opponentId);
}
