package com.shuttleup.backend.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {
    @Test
    void 自分の得点が高い場合は勝利になる() {
        Match match = Match.builder().myScore(21).opponentScore(18).build();
        assertThat(match.isWin()).isTrue();
        assertThat(match.isLoss()).isFalse();
    }

    @Test
    void 自分の得点が低い場合は敗北になる() {
        Match match = Match.builder().myScore(15).opponentScore(21).build();
        assertThat(match.isLoss()).isTrue();
        assertThat(match.isWin()).isFalse();
    }
}
