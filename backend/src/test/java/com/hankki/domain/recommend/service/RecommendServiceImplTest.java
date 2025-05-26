package com.hankki.domain.recommend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hankki.common.gpt.GptPromptBuilder;
import com.hankki.common.gpt.OpenAiApiService;
import com.hankki.domain.food.entity.Food;
import com.hankki.domain.food.repository.FoodRepository;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.dto.RagRecommendRequest;
import com.hankki.domain.user.constant.ActivityFactor;
import com.hankki.domain.user.constant.Gender;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.repository.UserHealthInfoRepository;

@ExtendWith(MockitoExtension.class)
class RecommendServiceImplTest {

    @Mock FoodRepository foodRepo;
    @Mock UserHealthInfoRepository healthRepo;
    @Mock RecommendFacade recommendFacade;
    @Mock GptPromptBuilder promptBuilder;
    @Mock OpenAiApiService openAi;

    @InjectMocks
    RecommendServiceImpl service;

    UserHealthInfo dummyInfo;

    @BeforeEach
    void setup() {
        // 기본 유저 헬스정보 스텁
        dummyInfo = UserHealthInfo.builder()
            .id(1L)
            .age(30)
            .gender(Gender.FEMALE)
            .height(165)
            .weight(60)
            .activityFactor(ActivityFactor.MODERATE)
            .build();
        when(healthRepo.findById(1L))
            .thenReturn(Optional.of(dummyInfo));
    }

    @Test
    void exactMatch단계에서찾으면바로반환() {
        // 1) GPT가 추천할 메뉴명
        when(promptBuilder.build(any(), any(), any(), any()))
            .thenReturn("PROMPT");
        when(openAi.requestChatCompletion("PROMPT"))
            .thenReturn("추천 음식 : 김치찌개");

        // 2) 정확 일치 스텁
        Food kimchi = Food.builder().id(10L).foodName("김치찌개").build();
        when(foodRepo.findByFoodNameIgnoreCase("김치찌개"))
            .thenReturn(Optional.of(kimchi));

        // 실행
        FoodResponseDto dto = service.recommendByRag(1L, new RagRecommendRequest(null));
        // 검증
        assert dto.getId() == 10L;
        assert "김치찌개".equals(dto.getFoodName());

        // 풀텍스트/토큰 매칭은 호출되지 않아야 함
        verify(foodRepo, never()).findBestMatchByFullText(any());
        verify(foodRepo, never()).findBestMatchByTokens(any());
    }

    @Test
    void fullText매칭단계에서찾으면그쪽반환() {
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("P");
        when(openAi.requestChatCompletion("P"))
            .thenReturn("추천 음식 : 불고기");

        when(foodRepo.findByFoodNameIgnoreCase("불고기"))
            .thenReturn(Optional.empty());

        Food bulgogi = Food.builder().id(20L).foodName("불고기").build();
        when(foodRepo.findBestMatchByFullText("불고기"))
            .thenReturn(Optional.of(bulgogi));

        FoodResponseDto dto = service.recommendByRag(1L, new RagRecommendRequest(null));
        assert dto.getId() == 20L;
        verify(foodRepo, never()).findBestMatchByTokens(any());
    }

    @Test
    void token매칭단계에서찾으면반환() {
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("P");
        when(openAi.requestChatCompletion("P"))
            .thenReturn("추천 음식 : 샐러드");

        when(foodRepo.findByFoodNameIgnoreCase("샐러드"))
            .thenReturn(Optional.empty());
        when(foodRepo.findBestMatchByFullText("샐러드"))
            .thenReturn(Optional.empty());

        Food salad = Food.builder().id(30L).foodName("그릭 샐러드").build();
        when(foodRepo.findBestMatchByTokens(List.of("샐러드")))
            .thenReturn(Optional.of(salad));

        FoodResponseDto dto = service.recommendByRag(1L, new RagRecommendRequest(null));
        assert dto.getId() == 30L;
    }

    @Test
    void 모두실패하면fallback호출() {
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("P");
        when(openAi.requestChatCompletion(any()))
            .thenReturn("추천 음식 : 없는메뉴");

        when(foodRepo.findByFoodNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(foodRepo.findBestMatchByFullText(any())).thenReturn(Optional.empty());
        when(foodRepo.findBestMatchByTokens(any())).thenReturn(Optional.empty());

        FoodResponseDto fallback = FoodResponseDto.builder()
        	    .id(99L)
        	    .foodName("떡볶이")
        	    // 나머지 필드는 기본값 그대로 두거나 명시적으로 설정
        	    .majorCategory(null)
        	    .subCategory(null)
        	    .servingSize(0.0)
        	    .kcal(0.0)
        	    .moisture(0.0)
        	    .carbohydrate(0.0)
        	    .protein(0.0)
        	    .fat(0.0)
        	    .sugar(0.0)
        	    .sodium(0.0)
        	    .cholesterol(0.0)
        	    .build();
        when(recommendFacade.recommendFurthest(1L, Gender.FEMALE))
            .thenReturn(fallback);

        FoodResponseDto dto = service.recommendByRag(1L, new RagRecommendRequest(null));
        assert dto.getId() == 99L;
    }

    @Test
    void parsing실패시재시도() {
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("P");

        // 첫 번째 호출: 실패 리턴, 두 번째 호출: 정상 리턴
        when(openAi.requestChatCompletion(anyString()))
            .thenReturn("아무 응답 없음", "추천 음식 : 김치찌개");

        // 정확 일치 스텁
        Food kimchi = Food.builder().id(10L).foodName("김치찌개").build();
        when(foodRepo.findByFoodNameIgnoreCase("김치찌개"))
            .thenReturn(Optional.of(kimchi));

        FoodResponseDto dto = service.recommendByRag(1L, new RagRecommendRequest(null));
        assertEquals(10L, dto.getId());
        assertEquals("김치찌개", dto.getFoodName());
    }
}
