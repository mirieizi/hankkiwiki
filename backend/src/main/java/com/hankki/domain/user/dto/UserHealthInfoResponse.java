package com.hankki.domain.user.dto;

import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.UserHealthInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserHealthInfoResponse {
    private Long id;
    private Gender gender;
    private int height;
    private int weight;
    private int age;
    private ActivityFactor activityFactor;
    private UserSummary user;

    @Getter
    @Builder
    public static class UserSummary {
        private Long id;
        private String email;
        private String nickname;
    }

    public static UserHealthInfoResponse from(UserHealthInfo info) {
        return UserHealthInfoResponse.builder()
            .id(info.getId())
            .gender(info.getGender())
            .height(info.getHeight())
            .weight(info.getWeight())
            .age(info.getAge())
            .activityFactor(info.getActivityFactor())
//            .user(UserSummary.builder()
//                .id(info.getUser().getId())
//                .email(info.getUser().getEmail())
//                .nickname(info.getUser().getNickname())
//                .build())
            .build();
    }
}
