package com.hankki.domain.recommend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public FoodResponseDto recommendRandomFood(Gender gender) {
        Food food = foodRepository.findRandomFood()
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodResponseDto findFoodDtoById(Long foodId) {
        Food food = foodRepository.findById(foodId)
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_FOOD));
        return FoodResponseDto.fromEntity(food);
    }

    @Override
    @Transactional
    public FoodResponseDto recommendByRag(Long userId, RagRecommendRequest request) {
        List<String> blackList = new ArrayList<>();

        // 사용자 건강정보 불러오기
        UserHealthInfo info = userHealthInfoRepository.findById(userId)
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH));

        // TODO: 최근 먹은 음식, 이전 추천 음식 목록 실제 데이터로 대체
        List<Food> recentFoods = null;
        List<String> triedFoods = null;

        String prompt = gptPromptBuilder.build(
            info,
            recentFoods,
            triedFoods,
            request != null ? request.getUserInput() : null
        );

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.info("RAG 시도 {}/{} – prompt: {}", attempt, MAX_RETRIES, prompt);

            String gptResp = openAiApiService.requestChatCompletion(prompt);
            String name;

            try {
                name = extractFoodNameFromGptResponse(gptResp);
            } catch (IllegalArgumentException ex) {
                log.warn("파싱 실패 (시도 {}): {}", attempt, ex.getMessage());
                prompt = prompt + "\n 응답 형식이 잘못됐습니다. '추천 음식 : [메뉴명]' 형식으로만 답변해주세요.";
                continue;
            }

            if (blackList.contains(name)) {
                log.info("중복 추천: {} (시도 {})", name, attempt);
                prompt = addExclusionToPrompt(prompt, blackList);
                continue;
            }
            blackList.add(name);

            // 1) 정확 일치
            Optional<Food> exact = foodRepository.findByFoodNameIgnoreCase(name);
            if (exact.isPresent()) {
                return FoodResponseDto.fromEntity(exact.get());
            }

            // 2) Full-Text 검색
            Optional<Food> fulltext = foodRepository.findBestMatchByFullText(name);
            if (fulltext.isPresent()) {
                return FoodResponseDto.fromEntity(fulltext.get());
            }

            // 3) 토큰 유사도 검색
            List<String> tokens = Arrays.asList(name.split("\\s+"));
            Optional<Food> fuzzy = foodRepository.findBestMatchByTokens(tokens);
            if (fuzzy.isPresent()) {
                return FoodResponseDto.fromEntity(fuzzy.get());
            }

            log.info("모든 매칭 실패: {} (시도 {})", name, attempt);
            prompt = addExclusionToPrompt(prompt, blackList);
        }

        // 3회 실패 시 예외 발생 (Facade에서 fallback 처리)
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
