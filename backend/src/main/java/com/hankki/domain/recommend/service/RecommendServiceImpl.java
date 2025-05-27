package com.hankki.domain.recommend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.common.gpt.GptPromptBuilder;
import com.hankki.common.gpt.OpenAiApiService;
import com.hankki.domain.diet.service.DietService;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.recommend.dto.RagRecommendResponse;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.repository.UserHealthInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendServiceImpl implements RecommendService {

    private static final int MAX_RETRIES = 3;

    private final FoodRepository foodRepository;
    private final UserHealthInfoRepository userHealthInfoRepository;
    private final GptPromptBuilder gptPromptBuilder;
    private final OpenAiApiService openAiApiService;
    private final DietService dietService;
    private final ObjectMapper objectMapper;  // Jackson

    @Override
    @Transactional(readOnly = true)
    public FoodResponseDto recommendRandomFood(Gender gender) {
        Food food = foodRepository.findRandomFood()
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food, "최근 먹은 음식과 가장 거리가 멀어요");
    }

    @Override
    @Transactional(readOnly = true)
    public FoodResponseDto findFoodDtoById(Long foodId) {
        Food food = foodRepository.findById(foodId)
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food, "음식 찾았어요");
    }

    @Override
    @Transactional
    public FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request) {
        List<String> blackList = new ArrayList<>();

        // 사용자 건강정보 불러오기
        UserHealthInfo info = userHealthInfoRepository.findByUserId(userId)
                .orElse(null);
        
        List<Food> recentFoods = dietService.findRecentFoods(userId, 3);
        List<String> triedFoods = null;
        Food furthestFood = null;
        log.info("DEBUG - 빌더 호출 직전 prefer='{}', avoid='{}'", 
                request != null ? request.getPrefer() : "request==null", 
                request != null ? request.getAvoid() : "request==null");
        String prompt = gptPromptBuilder.build(
            info, recentFoods, triedFoods, furthestFood,
            request != null ? request.getPrefer() : null,
            request != null ? request.getAvoid()  : null
        );
        log.info(request.getAvoid()+" "+request.getPrefer());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.info("RAG 시도 {}/{} – prompt:\n{}", attempt, MAX_RETRIES, prompt);

            String gptResp = openAiApiService.requestChatCompletion(prompt);
            log.info("→ GPT raw 응답 (length={}):\n{}", gptResp.length(), gptResp);

            // JSON 파싱
            RagRecommendResponse gptResult;
            try {
                gptResult = objectMapper.readValue(gptResp, RagRecommendResponse.class);
            } catch (Exception e) {
                log.warn("JSON 파싱 실패 (시도 {}): {}\nraw: {}", attempt, e.getMessage(), gptResp);
                prompt += "\n응답은 아래 JSON 형식으로만 주세요:\n"
                        + "{\"recommendation\":\"추천 음식 : [메뉴명]\",\"reason\":\"[추천 이유]\"}";
                continue;
            }

            String rawRec = gptResult.getRecommendation();
            String name   = rawRec.replaceFirst("추천 음식\\s*[:：]\\s*", "").trim();
            String reason = gptResult.getReason();
            log.info("→ 파싱된 recommendation='{}', reason='{}'", name, reason);

            if (blackList.contains(name)) {
                log.info("→ 중복 추천 감지: '{}' (시도 {})", name, attempt);
                prompt = addExclusionToPrompt(prompt, blackList);
                continue;
            }
            blackList.add(name);

            // 1) 정확 일치
            Optional<Food> exact = foodRepository.findByFoodNameIgnoreCase(name);
            if (exact.isPresent()) {
                Food f = exact.get();
                log.info("✔ 매칭 단계: EXACT (완전 일치) → id={}, name={}", f.getId(), f.getFoodName());
                return FoodResponseDto.fromEntity(f, reason);
            }

            // 2) Full-Text 검색
            Optional<Food> fulltext = foodRepository.findBestMatchByFullText(name);
            if (fulltext.isPresent()) {
                Food f = fulltext.get();
                log.info("✔ 매칭 단계: FULL-TEXT INDEX → id={}, name={}", f.getId(), f.getFoodName());
                return FoodResponseDto.fromEntity(f, reason);
            }

            // 3) 토큰 유사도 검색
            List<String> tokens = Arrays.asList(name.split("\\s+"));
            log.info("→ 토큰 유사도 검색용 tokens={}", tokens);
            Optional<Food> fuzzy = foodRepository.findBestMatchByTokens(tokens);
            if (fuzzy.isPresent()) {
                Food f = fuzzy.get();
                log.info("✔ 매칭 단계: TOKEN MATCH → id={}, name={}", f.getId(), f.getFoodName());
                return FoodResponseDto.fromEntity(f, reason);
            }

            log.info("✖ 모든 매칭 실패: '{}' (시도 {})", name, attempt);
            prompt = addExclusionToPrompt(prompt, blackList);
        }

        throw new HankkiWikiException(ExceptionStatus.RECOMMEND_FAIL);
    }


    private String addExclusionToPrompt(String original, List<String> tried) {
        if (tried.isEmpty()) return original;
        return original + "\n이전 추천된 음식(" +
               String.join(", ", tried) +
               ")은 제외하고 다른 음식 추천해줘.";
    }

    private String extractFoodNameFromGptResponse(String resp) {
        Pattern p = Pattern.compile("추천 음식\\s*[:：]\\s*(.+)", Pattern.DOTALL);
        Matcher m = p.matcher(resp);
        if (m.find()) {
            return m.group(1)
                    .trim()
                    .replaceAll("[\\r\\n]", "")
                    .replaceAll("[.。！!]*$", "");
        }
        throw new IllegalArgumentException("음식명 추출 실패: " + resp);
    }
}
