package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Opponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpponentRepository extends JpaRepository<Opponent, Long> {
}
