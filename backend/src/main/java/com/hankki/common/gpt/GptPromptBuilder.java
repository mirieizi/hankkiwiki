package com.hankki.common.gpt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.user.entity.UserHealthInfo;

/**
 * 건강정보, 최근 식사, 벡터최외곽, 선호/비선호를 기반으로
 * JSON 형식의 추천 결과와 이유를 요청하는 프롬프트 제작
 */
@Component
public class GptPromptBuilder {

    /**
     * @param healthInfo   사용자 건강정보
     * @param recentFoods  최근에 먹은 음식 목록 (null 허용)
     * @param excludeFoods 이전에 추천된 음식명(시도 목록) (null 허용)
     * @param furthestFood 벡터상 가장 멀리 떨어진 음식 (null 허용)
     * @param prefer       사용자가 선호하는 음식/스타일 (null 허용)
     * @param avoid        사용자가 피하고 싶은 음식/재료 (null 허용)
     */
    public String build(UserHealthInfo healthInfo,
                        List<Food> recentFoods,
                        List<String> excludeFoods,
                        Food furthestFood,
                        String prefer,
                        String avoid) {

        StringBuilder sb = new StringBuilder();

        // 1) 건강정보
        if (healthInfo != null) {
            sb.append("아래는 사용자의 건강정보입니다.\n")
              .append("- 나이: ").append(healthInfo.getAge()).append("\n")
              .append("- 성별: ").append(healthInfo.getGender()).append("\n")
              .append("- 키: ").append(healthInfo.getHeight()).append("cm\n")
              .append("- 몸무게: ").append(healthInfo.getWeight()).append("kg\n")
              .append("- 활동량: ").append(healthInfo.getActivityFactor()).append("\n\n");
        } else {
            sb.append("사용자 건강정보가 제공되지 않았습니다.\n\n");
        }


        // 2) 최근 식사 정보
        if (recentFoods != null && !recentFoods.isEmpty()) {
            sb.append("아래는 최근에 드신 음식입니다. 최대한 !!!겹치지 않는 음식!!!을 추천해주세요:\n");
            for (Food f : recentFoods) {
                sb.append("- ").append(f.getFoodName())
                  .append(" (칼로리: ").append(f.getKcal()).append("kcal, 단백질: ")
                  .append(f.getProtein()).append("g, 탄수화물: ")
                  .append(f.getCarbohydrate()).append("g, 지방: ")
                  .append(f.getFat()).append("g)\n");
            }
            sb.append("\n");
        }

        // 3) 이전 시도한 음식 제외
        if (excludeFoods != null && !excludeFoods.isEmpty()) {
            sb.append("이전 추천된 음식(")
              .append(String.join(", ", excludeFoods))
              .append(")은 제외하고 추천해주세요.\n\n");
        }
        // 4) 벡터 최외곽 음식 참고
        if (furthestFood != null) {
            sb.append("최대 다양성을 위해 벡터상 가장 거리가 먼 음식은 '")
              .append(furthestFood.getFoodName())
              .append("'입니다. 참고만 해주세요.\n\n");
        }

        // 5) 선호/비선호 반영
        if (prefer != null && !prefer.isBlank()) {
            sb.append("사용자가 선호하는 음식/스타일: ").append(prefer).append("\n");
        }
        if (avoid != null && !avoid.isBlank()) {
            sb.append("사용자가 피하고 싶은 음식/재료: ").append(avoid).append("\n");
        }
        if ((prefer != null && !prefer.isBlank()) || (avoid != null && !avoid.isBlank())) {
            sb.append("\n");
        }

        // 6) JSON 형식 응답 요청
        sb.append("위 정보를 참고해서 사용자가 먹은 음식과 최대한 겹치지 않는 음식을 참고하고, 오늘 먹기 좋은 건강한 식사를 한 가지 추천하고, 사용자가 선호하는 음식과 그 피하고 싶은 이유들을 반드시 반영해주세요. 그 이유를 간단히 설명해주세요.\n");
        sb.append("응답은 반드시 JSON 형식으로만 해주세요. 예시:\n");
        sb.append("{\n");
        sb.append("  \"recommendation\": \"추천 음식 : [메뉴명]\",\n");
        sb.append("  \"reason\": \"[추천 이유를 한 문장으로]\"\n");
        sb.append("}");

        return sb.toString();
    }
}
