package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 全項目任意。null（省略を含む）は現在値を消去する。 */
@Getter
@Setter
public class BodyDataUpdateRequest {
    @DecimalMin(value = "0.01", message = "身長は0.01〜999.99 cmで入力してください。")
    @DecimalMax(value = "999.99", message = "身長は0.01〜999.99 cmで入力してください。")
    @Digits(integer = 3, fraction = 2, message = "身長は小数点以下2桁以内で入力してください。")
    private BigDecimal heightCm;

    @DecimalMin(value = "0.01", message = "体重は0.01〜999.99 kgで入力してください。")
    @DecimalMax(value = "999.99", message = "体重は0.01〜999.99 kgで入力してください。")
    @Digits(integer = 3, fraction = 2, message = "体重は小数点以下2桁以内で入力してください。")
    private BigDecimal bodyWeightKg;

    @DecimalMin(value = "0", message = "体脂肪率は0〜100 %で入力してください。")
    @DecimalMax(value = "100", message = "体脂肪率は0〜100 %で入力してください。")
    @Digits(integer = 3, fraction = 2, message = "体脂肪率は小数点以下2桁以内で入力してください。")
    private BigDecimal bodyFatPercentage;
}
