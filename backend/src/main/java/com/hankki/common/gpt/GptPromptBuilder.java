package com.hankki.common.gpt;

import java.util.List;

import com.hankki.domain.food.entity.Food;
import com.hankki.domain.user.entity.UserHealthInfo;
import org.springframework.stereotype.Component;

/**
 * 건강정보, 최근 음식, 유저 입력(옵션)을 받아서 open ai에 전달할 프롬프트 제작
 */
@Component
public class GptPromptBuilder {
	public static String build(UserHealthInfo healthInfo, List<Food> recentFoods, String userInput) {
		StringBuilder sb = new StringBuilder();
		sb.append("아래는 사용자의 건강정보입니다.\n");
        sb.append("- 나이: ").append(healthInfo.getAge()).append("\n");
        sb.append("- 성별: ").append(healthInfo.getGender()).append("\n");
        sb.append("- 키: ").append(healthInfo.getHeight()).append("cm\n");
        sb.append("- 몸무게: ").append(healthInfo.getWeight()).append("kg\n");
        sb.append("- 활동량: ").append(healthInfo.getActivityFactor()).append("\n\n");
        
        sb.append("아래는 최근에 먹은 음식과 영양소입니다. 이 음식 종류와 최대한 겹치지 않는 음식을 추천해야 합니다.\n");
        for (Food food : recentFoods) {
            sb.append("- ").append(food.getFoodName())
                .append(" (칼로리: ").append(food.getKcal())
                .append("kcal, 단백질: ").append(food.getProtein())
                .append("g, 탄수화물: ").append(food.getCarbohydrate())
                .append("g, 지방: ").append(food.getFat())
                .append("g, 당: ").append(food.getSugar()).append("g");
            if (food.getSodium() > 0) sb.append(", 나트륨: ").append(food.getSodium()).append("mg");
            if (food.getCholesterol() > 0) sb.append(", 콜레스테롤: ").append(food.getCholesterol()).append("mg");
            sb.append(")\n");
        }
        if (userInput != null && !userInput.isBlank()) {
        	sb.append("\n유저의 추가 요청입니다.").append(userInput).append(userInput).append("\n");
        }
        sb.append("\n이 정보를 참고해서 오늘 먹기 좋은 건강한 식사를 한 가지 추천해줘. 음식 이름과 추천 이유도 같이 알려줘");
        return sb.toString();
	}
}
