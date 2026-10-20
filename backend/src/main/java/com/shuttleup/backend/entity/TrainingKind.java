package com.shuttleup.backend.entity;

import java.text.Normalizer;
import java.util.List;

/** 表示名から独立した、作成後は変更しない統計区分。 */
public enum TrainingKind {
    PHYSICAL, SKILL, OTHER;

    /** 既存データだけに適用する旧分類ルール。 */
    public static TrainingKind fromLegacyName(String name) {
        String value = Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFKC);
        boolean physical = List.of("フィジカル", "筋力", "体力", "ウエイト", "ウェイト",
                "ストレングス", "コンディショニング", "有酸素").stream().anyMatch(value::contains);
        boolean skill = List.of("バドミントン", "技術", "スキル", "ノック", "フットワーク",
                "ショット", "ラケット").stream().anyMatch(value::contains);
        return physical == skill ? OTHER : physical ? PHYSICAL : SKILL;
    }
}
