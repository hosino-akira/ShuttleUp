package com.shuttleup.backend.service;

import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisDataServiceTest {
    @Mock UserRepository userRepository;
    @Mock TrainingSessionRepository sessionRepository;
    @Mock TrainingRecordRepository recordRepository;
    @Mock MatchRepository matchRepository;
    AnalysisDataService service;

    @BeforeEach
    void 準備() {
        service = new AnalysisDataService(userRepository, sessionRepository, recordRepository, matchRepository);
    }

    @Test
    void 指定期間とユーザーで全データを制限する() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 31);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateAscIdAsc(1L, from, to))
                .thenReturn(List.of());
        when(recordRepository.findAnalysisRecords(1L, from, to)).thenReturn(List.of());
        when(matchRepository.findAnalysisMatches(1L, from, to)).thenReturn(List.of());

        var response = service.getAnalysisData(1L, from, to);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getSessions()).isEmpty();
        verify(recordRepository).findAnalysisRecords(1L, from, to);
        verify(matchRepository).findAnalysisMatches(1L, from, to);
    }

    @Test
    void 開始日が終了日より後なら入力エラー() {
        assertThatThrownBy(() -> service.getAnalysisData(1L,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 31)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 存在しないユーザーは404用例外() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.getAnalysisData(99L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
