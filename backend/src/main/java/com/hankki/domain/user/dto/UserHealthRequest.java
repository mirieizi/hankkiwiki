package com.hankki.domain.user.dto;

import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;
import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHealthRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Gender gender;

    @NotNull @Min(50) @Max(300)
    private Integer height;

    @NotNull @Min(1) @Max(500)
    private Integer weight;

    @NotNull @Min(0) @Max(150)
    private Integer age;

    @NotNull
    private ActivityFactor activityFactor;

    @NotNull @Min(1) @Max(5)
    private Integer dailyUsage;
}
