package com.shuttleup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/** 共通プリセットに対する本人だけの名前・有効状態。元のIDは変更しない。 */
@Entity
@Table(name = "exercise_master_settings", uniqueConstraints = @UniqueConstraint(
        name = "uk_master_setting_user_target", columnNames = {"user_id", "target_kind", "target_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ExerciseMasterSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_kind", nullable = false, length = 20)
    private MasterKind targetKind;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean enabled;
}
