package com.hankki.common.gpt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.user.entity.UserHealthInfo;

@Component
public class GptPromptBuilder {

    /**
     * @param healthInfo   사용자 건강정보
     * @param recentFoods  최근에 먹은 음식 목록 (null 허용)
     * @param excludeFoods 이전에 추천된 음식명(시도 목록) (null 허용)
     * @param userInput    유저 추가 요청 (null 허용)
     */
    public String build(UserHealthInfo healthInfo,
                        List<Food> recentFoods,
                        List<String> excludeFoods,
                        String userInput) {

        StringBuilder sb = new StringBuilder();

        // 1) 건강정보
        sb.append("아래는 사용자의 건강정보입니다.\n")
          .append("- 나이: ").append(healthInfo.getAge()).append("\n")
          .append("- 성별: ").append(healthInfo.getGender()).append("\n")
          .append("- 키: ").append(healthInfo.getHeight()).append("cm\n")
          .append("- 몸무게: ").append(healthInfo.getWeight()).append("kg\n")
          .append("- 활동량: ").append(healthInfo.getActivityFactor()).append("\n\n");

        // 2) 최근 식사 정보
        if (recentFoods != null && !recentFoods.isEmpty()) {
            sb.append("아래는 최근에 드신 음식입니다. 최대한 겹치지 않는 음식을 추천해주세요:\n");
            for (Food f : recentFoods) {
                sb.append("- ").append(f.getFoodName())
                  .append(" (칼로리: ").append(f.getKcal()).append("kcal, 단백질: ")
                  .append(f.getProtein()).append("g, 탄수화물: ").append(f.getCarbohydrate())
                  .append("g, 지방: ").append(f.getFat()).append("g)\n");
            }
            sb.append("\n");
        }

        // 3) 이전 시도한 음식 제외
        if (excludeFoods != null && !excludeFoods.isEmpty()) {
            sb.append("이전 추천된 음식(")
              .append(String.join(", ", excludeFoods))
              .append(")은 제외하고 추천해주세요.\n\n");
        }

        // 4) 유저 추가 요청
        if (userInput != null && !userInput.isBlank()) {
            sb.append("유저 요청: ").append(userInput).append("\n\n");
        }

        // 5) 최종 요청
        sb.append("위 정보를 참고해서 오늘 먹기 좋은 건강한 식사를 하나 추천해주세요.\n")
          .append("추천 음식명을 대답할 때는 반드시 “추천 음식 : [메뉴명]” 형식으로만 응답해주세요.");

        return sb.toString();
    }
}
