package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.OpponentCreateRequest;
import com.shuttleup.backend.dto.request.OpponentUpdateRequest;
import com.shuttleup.backend.dto.response.OpponentResponse;
import com.shuttleup.backend.entity.Opponent;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpponentService {

    private final OpponentRepository opponentRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public OpponentService(
            OpponentRepository opponentRepository,
            UserRepository userRepository,
            MatchRepository matchRepository) {
        this.opponentRepository = opponentRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
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
        User user = findUser(userId);
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
        Opponent opponent = findOpponent(opponentId);
        String name = request.getName().trim();
        Long userId = opponent.getUser().getId();
        if (opponentRepository.existsByUserIdAndNameAndIdNot(
                userId, name, opponentId)) {
            throw duplicateNameException();
        }

        opponent.setName(name);
        opponent.setMemo(normalizeMemo(request.getMemo()));
        opponent.setUpdatedAt(LocalDateTime.now());
        return toResponse(opponentRepository.save(opponent));
    }

    /** 試合で未使用の対戦相手を削除する。 */
    @Transactional
    public void deleteOpponent(Long opponentId) {
        Opponent opponent = findOpponent(opponentId);
        if (matchRepository.existsByOpponentId(opponentId)) {
            throw new IllegalStateException(
                    "この対戦相手は試合記録で使用されているため削除できません。");
        }
        opponentRepository.delete(opponent);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたユーザーが見つかりません。"));
    }

    private Opponent findOpponent(Long opponentId) {
        return opponentRepository.findById(opponentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された対戦相手が見つかりません。"));
    }

    private IllegalStateException duplicateNameException() {
        return new IllegalStateException(
                "同じ名前の対戦相手が既に登録されています。");
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
                .memo(opponent.getMemo())
                .createdAt(opponent.getCreatedAt())
                .updatedAt(opponent.getUpdatedAt())
                .build();
    }
}
