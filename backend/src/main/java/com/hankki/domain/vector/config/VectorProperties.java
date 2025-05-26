package com.hankki.domain.vector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vector.redis")
@Getter
@Setter
public class VectorProperties {

    private int dimension = 9;
    private int searchTimeout = 5000;
    private int batchSize = 100;
    private String indexPrefix = "idx_";
    private String keyPrefix = "food_";
}
