package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.OpponentCreateRequest;
import com.shuttleup.backend.dto.request.OpponentUpdateRequest;
import com.shuttleup.backend.dto.response.OpponentResponse;
import com.shuttleup.backend.entity.Opponent;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.UserRepository;
import com.shuttleup.backend.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpponentService {

    private final OpponentRepository opponentRepository;
    private final UserRepository userRepository;

    public OpponentService(
            OpponentRepository opponentRepository,
            UserRepository userRepository) {
        this.opponentRepository = opponentRepository;
        this.userRepository = userRepository;
    }

    /** 指定されたユーザーが登録した対戦相手一覧を名前順で取得する。 */
    @Transactional(readOnly = true)
    public List<OpponentResponse> getOpponentsByUserId(Long userId) {
        findUser(userId);
        return opponentRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** IDを指定して対戦相手を取得する。 */
    @Transactional(readOnly = true)
    public OpponentResponse getOpponent(Long opponentId) {
        return toResponse(findOpponent(opponentId));
    }

    /** 指定されたユーザーに対戦相手を登録する。 */
    @Transactional
    public OpponentResponse createOpponent(
            Long userId,
            OpponentCreateRequest request) {
        CurrentUser.requireOwner(userId);
        User user = userRepository.findByIdForUpdate(userId).orElseThrow();
        String name = request.getName().trim();
        if (opponentRepository.existsByUserIdAndName(userId, name)) {
            throw duplicateNameException();
        }

        LocalDateTime now = LocalDateTime.now();
        Opponent opponent = new Opponent();
        opponent.setUser(user);
        opponent.setName(name);
        opponent.setMemo(normalizeMemo(request.getMemo()));
        opponent.setCreatedAt(now);
        opponent.setUpdatedAt(now);
        return toResponse(opponentRepository.save(opponent));
    }

    /** 対戦相手の名前とメモを更新する。 */
    @Transactional
    public OpponentResponse updateOpponent(
            Long opponentId,
            OpponentUpdateRequest request) {
        userRepository.findByIdForUpdate(CurrentUser.id()).orElseThrow();
        Opponent opponent = findOpponent(opponentId);
        String name = request.getName().trim();
        Long userId = opponent.getUser().getId();
        if (opponentRepository.existsByUserIdAndNameAndIdNot(
                userId, name, opponentId)) {
            throw duplicateNameException();
        }

        opponent.setName(name);
        opponent.setMemo(normalizeMemo(request.getMemo()));
        if (request.getEnabled() != null) opponent.setEnabled(request.getEnabled());
        opponent.setUpdatedAt(LocalDateTime.now());
        return toResponse(opponentRepository.save(opponent));
    }

    private User findUser(Long userId) {
        CurrentUser.requireOwner(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたユーザーが見つかりません。"));
    }

    private Opponent findOpponent(Long opponentId) {
        Opponent opponent = opponentRepository.findById(opponentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された対戦相手が見つかりません。"));
        CurrentUser.requireOwner(opponent.getUser().getId());
        return opponent;
    }

    private IllegalStateException duplicateNameException() {
        return new IllegalStateException(
                "同じ名前の対戦相手が既に登録されています。無効な項目は再度有効にするか、別の名前を入力してください。");
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.trim();
    }

    private OpponentResponse toResponse(Opponent opponent) {
        return OpponentResponse.builder()
                .id(opponent.getId())
                .userId(opponent.getUser().getId())
                .name(opponent.getName())
                .enabled(opponent.getEnabled()).available(opponent.getEnabled())
                .memo(opponent.getMemo())
                .createdAt(opponent.getCreatedAt())
                .updatedAt(opponent.getUpdatedAt())
                .build();
    }
}
