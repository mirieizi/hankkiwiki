package com.hankki.domain.recommend.controller;

import com.hankki.common.security.principal.CurrentUser;
import com.hankki.domain.auth.dto.UserPrincipal;
import com.hankki.domain.recommend.dto.FoodResponseDto;
import com.hankki.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

//    private final RecommendService recommendService;
//
//    @GetMapping("/random")
//    public ResponseEntity<FoodResponseDto> recommendRandom(
//            @CurrentUser UserPrincipal userPrincipal
//    ) {
//
//    }
}
