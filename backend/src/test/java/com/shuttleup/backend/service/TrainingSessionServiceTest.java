package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.TrainingSessionCreateRequest;
import com.shuttleup.backend.dto.request.TrainingSessionUpdateRequest;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest extends AuthenticatedServiceTest {
    @Mock TrainingSessionRepository sessions;
    @Mock UserRepository users;

    private TrainingSessionService service() { return new TrainingSessionService(sessions, users); }
    private TrainingSessionCreateRequest createRequest(String plantType) {
        var request = new TrainingSessionCreateRequest();
        request.setUserId(1L);
        request.setTrainingDate(LocalDate.of(2026, 10, 13));
        request.setDurationMinutes(60);
        request.setPlantType(plantType);
        return request;
    }
    private TrainingSessionUpdateRequest updateRequest(String plantType) {
        var request = new TrainingSessionUpdateRequest();
        request.setTrainingDate(LocalDate.of(2026, 10, 13));
        request.setDurationMinutes(90);
        request.setPlantType(plantType);
        return request;
    }
    private TrainingSession existing(String plantType) {
        return TrainingSession.builder().id(10L).user(User.builder().id(1L).build())
                .trainingDate(LocalDate.of(2026, 10, 13)).durationMinutes(60).plantType(plantType).build();
    }

    @Test
    void selectedPlantIsStoredWithTraining() {
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(sessions.save(any())).thenAnswer(call -> {
            TrainingSession saved = call.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        var response = service().createTrainingSession(createRequest("sakura"));
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getPlantType()).isEqualTo("sakura");
    }

    @Test
    void oldClientsCanCreateTrainingWithoutSpecifyingPlant() {
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(sessions.save(any())).thenAnswer(call -> call.getArgument(0));
        assertThat(service().createTrainingSession(createRequest(null)).getPlantType()).isEqualTo("cedar");
    }

    @Test
    void legacyTrainingAppearsAsCedarWithoutChangingTheRecord() {
        var legacy = existing(null);
        when(sessions.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(legacy));
        assertThat(service().getTrainingSessions(1L).getFirst().getPlantType()).isEqualTo("cedar");
        assertThat(legacy.getPlantType()).isNull();
        verify(sessions, never()).save(any());
    }

    @Test
    void editingTrainingPreservesThePlantWhenOldClientOmitsIt() {
        var existing = existing("lavender");
        when(sessions.findById(10L)).thenReturn(Optional.of(existing));
        when(sessions.save(existing)).thenReturn(existing);
        var response = service().updateTrainingSession(10L, updateRequest(null));
        assertThat(response.getPlantType()).isEqualTo("lavender");
        assertThat(response.getDurationMinutes()).isEqualTo(90);
        assertThat(response.getId()).isEqualTo(10L);
        verify(sessions).save(existing);
    }

    @Test
    void changingSpeciesUpdatesTheSameTrainingInsteadOfCreatingAnotherPlant() {
        var existing = existing("cedar");
        when(sessions.findById(10L)).thenReturn(Optional.of(existing));
        when(sessions.save(existing)).thenReturn(existing);
        var response = service().updateTrainingSession(10L, updateRequest("sunflower"));
        assertThat(response.getPlantType()).isEqualTo("sunflower");
        assertThat(response.getId()).isEqualTo(10L);
        verify(sessions).save(existing);
        verifyNoInteractions(users);
    }

    @Test
    void requestsAcceptOnlySupportedSpeciesOrAnOmittedSpecies() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String type : List.of("cedar", "oak", "sakura", "maple", "sunflower", "lavender")) {
                assertThat(validator.validate(createRequest(type))).isEmpty();
                assertThat(validator.validate(updateRequest(type))).isEmpty();
            }
            assertThat(validator.validate(createRequest(null))).isEmpty();
            assertThat(validator.validate(updateRequest(null))).isEmpty();
            for (String type : List.of("", "unknown", "SAKURA", "__proto__")) {
                assertThat(validator.validate(createRequest(type))).anyMatch(v -> v.getPropertyPath().toString().equals("plantType"));
                assertThat(validator.validate(updateRequest(type))).anyMatch(v -> v.getPropertyPath().toString().equals("plantType"));
            }
        }
    }
}
