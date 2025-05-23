package com.hankki.domain.recommend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.common.gpt.GptPromptBuilder;
import com.hankki.common.gpt.OpenAiApiService;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.repository.UserHealthInfoRepository;

import java.util.regex.Matcher;
import jakarta.transaction.Transactional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final FoodRepository foodRepository;
    private final UserHealthInfoRepository userHealthInfoRepository;
    private final RecommendVectorFacade recommendVectorFacade;
    private final GptPromptBuilder gptPromptBuilder;
    private final OpenAiApiService openAiApiService;

    /**
     * 무작위 음식 1개 추천
     */
    @Transactional
    @Override
    public FoodResponseDto recommendRandomFood(Gender gender) {
        Food food = foodRepository.findRandomFood()
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food);
    }

    /**
     * 음식 ID로 조회 후 DTO 변환
     */
    @Transactional
    @Override
    public FoodResponseDto findFoodDtoById(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalStateException("해당 음식이 존재하지 않습니다."));
        return FoodResponseDto.fromEntity(food);
    }
    /**
     * User 건강정보 조회, User 최근에 먹은 음식 조회, RAG 반환
     */
    @Override
    public FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request) {
        UserHealthInfo healthInfo = userHealthInfoRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("해당하는 유저의 건강정보가 존재하지 않습니다."));

        // 지금 최근 먹은 음식이 private하게 선언되어 있어서 그거 회의해보기... 우선 null
//        List<Food> recentFoods = recommendVectorFacade.loadRecentFoodIds(userId);
        String prompt = GptPromptBuilder.build(healthInfo, null, request != null ? request.getUserInput() : null);

        String gptResponse = openAiApiService.requestChatCompletion(prompt);

        // 후처리 메서드 호출 (파싱, DB 검증, 재요청)
        return handleGptResponseWithRetry(userId, request, gptResponse, new ArrayList<>(), 3);
    }

    /**
     * DB에서 정보를 찾지 못했을 때, 최대 3회 다시 요청하기
     * @param userId
     * @param request
     * @param gptResponse
     * @param triedFoods
     * @param retriesLeft
     * @return
     */
    private FoodResponseDto handleGptResponseWithRetry(Long userId, RagRecommendRequest request, String gptResponse, List<String> triedFoods, int retriesLeft) {
        if (retriesLeft <= 0) {
            throw new IllegalStateException("추천 가능한 음식이 없습니다. 재시도 횟수 초과");
        }

        String recommendedFoodName = extractFoodNameFromGptResponse(gptResponse);

        if (triedFoods.contains(recommendedFoodName)) {
            throw new IllegalStateException("추천 음식이 중복됩니다.");
        }
        triedFoods.add(recommendedFoodName);

        return foodRepository.findByFoodName(recommendedFoodName)
            .map(food -> FoodResponseDto.fromEntity(food))
            .orElseGet(() -> {
                // 재요청용 프롬프트 생성 (기존 healthInfo, 최근 음식 null 유지)
                UserHealthInfo healthInfo = userHealthInfoRepository.findById(userId).get();
                String newPrompt = GptPromptBuilder.build(healthInfo, null, request != null ? request.getUserInput() : null)
                    + "\n 이전에 추천된 음식 " + String.join(", ", triedFoods) + " 는 제외하고 다른 음식 추천해줘.";
                String newGptResponse = openAiApiService.requestChatCompletion(newPrompt);
                return handleGptResponseWithRetry(userId, request, newGptResponse, triedFoods, retriesLeft - 1);
            });
    }
    
    private String extractFoodNameFromGptResponse(String gptResponse) {
        Pattern pattern = Pattern.compile("추천 음식\\s*[:：]\\s*(.+)");
        Matcher matcher = pattern.matcher(gptResponse);
        if (matcher.find()) {
            // 줄 끝까지 잡을 수도 있지만, 필요하면 공백, 개행 등으로 끝내기
            String foodName = matcher.group(1).trim();
            // 필요시 음식명 뒤에 붙은 마침표나 문장부호 제거
            foodName = foodName.replaceAll("[.。！!]*$", "");
            return foodName;
        }
        throw new IllegalArgumentException("음식명 추출 실패: " + gptResponse);
    }


    
    
    

}
