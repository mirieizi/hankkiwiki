package com.hankki.domain.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RagRecommendResponse {
    @JsonProperty("recommendation")
    private String recommendation;

    @JsonProperty("reason")
    private String reason;
}

