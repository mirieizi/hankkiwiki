package com.hankki.domain.vector.util;

import java.util.List;

public class VectorAggregator {

    /**
     * 주어진 float 벡터 리스트를 평균 벡터로 그룹화
     */
    public static double[] average(List<double[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            throw new IllegalArgumentException("벡터 리스트가 비어있습니다.");
        }

        int dim = vectors.get(0).length;
        double[] avg = new double[dim];

        for (double[] vec : vectors) {
            for (int i = 0; i < dim; i++) {
                avg[i] += vec[i];
            }
        }

        for (int i = 0; i < dim; i++) {
            avg[i] /= vectors.size();
        }

        return avg;
    }
}
