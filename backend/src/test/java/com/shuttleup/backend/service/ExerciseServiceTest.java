package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.ExerciseCreateRequest;
import com.shuttleup.backend.dto.response.ExerciseResponse;
import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {
    @Mock ExerciseRepository exerciseRepository;
    @Mock ExerciseTypeRepository exerciseTypeRepository;
    @Mock UserRepository userRepository;

    @Test
    void ユーザー作成種目はシステムプリセットではなく作成ユーザーに関連付く() {
        ExerciseCategory category = ExerciseCategory.builder().id(1L).name("フィジカル").build();
        ExerciseType type = ExerciseType.builder().id(2L).name("筋力").category(category).build();
        User user = User.builder().id(1L).name("テストユーザー").build();
        when(exerciseTypeRepository.findById(2L)).thenReturn(Optional.of(type));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(exerciseRepository.save(org.mockito.ArgumentMatchers.any(Exercise.class)))
                .thenAnswer(invocation -> {
                    Exercise exercise = invocation.getArgument(0);
                    exercise.setId(10L);
                    return exercise;
                });
        ExerciseCreateRequest request = new ExerciseCreateRequest();
        request.setExerciseTypeId(2L);
        request.setUserId(1L);
        request.setName("  ケトルベルスイング  ");

        ExerciseResponse response = service().createExercise(request);

        ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
        verify(exerciseRepository).save(captor.capture());
        assertThat(captor.getValue().getSystemPreset()).isFalse();
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getName()).isEqualTo("ケトルベルスイング");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void 一覧取得は種別とユーザーをRepositoryへ渡す() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(exerciseRepository.findAvailableExercises(3L, 1L)).thenReturn(List.of());

        assertThat(service().getAvailableExercises(3L, 1L)).isEmpty();

        verify(exerciseRepository).findAvailableExercises(3L, 1L);
    }

    private ExerciseService service() {
        return new ExerciseService(exerciseRepository, exerciseTypeRepository, userRepository);
    }
}
