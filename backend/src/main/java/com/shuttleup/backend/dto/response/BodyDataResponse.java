package com.shuttleup.backend.dto.response;

import com.shuttleup.backend.entity.User;
import java.math.BigDecimal;

/** 認証本人専用。ログイン・アカウント情報の応答には含めない。 */
public record BodyDataResponse(BigDecimal heightCm, BigDecimal bodyWeightKg, BigDecimal bodyFatPercentage) {
    public static BodyDataResponse from(User user) {
        return new BodyDataResponse(user.getHeightCm(), user.getBodyWeightKg(), user.getBodyFatPercentage());
    }
}
