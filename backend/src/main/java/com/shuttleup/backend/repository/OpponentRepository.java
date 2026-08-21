package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Opponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpponentRepository extends JpaRepository<Opponent, Long> {

    List<Opponent> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndNameAndIdNot(Long userId, String name, Long id);
}
