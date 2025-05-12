package com.hankki.domain.user.dto;

import com.hankki.domain.user.entity.ActivityFactor;
import com.hankki.domain.user.entity.Gender;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.entity.UserHealthInfo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddInfoRequest {

    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @NotNull(message = "키를 입력해주세요.")
    @Min(value = 50, message = "키는 최소 50cm 이상이어야 합니다.")
    @Max(value = 300, message = "키는 최대 300cm 이하여야 합니다.")
    private Integer height;

    @NotNull(message = "몸무게를 입력해주세요.")
    @Min(value = 1, message = "몸무게는 최소 1kg 이상이어야 합니다.")
    @Max(value = 500, message = "몸무게는 최대 500kg 이하여야 합니다.")
    private Integer weight;

    @NotNull(message = "나이를 입력해주세요.")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 최대 150 이하여야 합니다.")
    private Integer age;

    @NotNull(message = "활동 레벨을 선택해주세요.")
    private ActivityFactor activityFactor;


    public UserHealthInfo toHealthInfo(User user) {
        return UserHealthInfo.builder()
            .user(user)
            .gender(this.gender)
            .height(this.height)
            .weight(this.weight)
            .age(this.age)
            .activityFactor(this.activityFactor)
            .build();
    }
}