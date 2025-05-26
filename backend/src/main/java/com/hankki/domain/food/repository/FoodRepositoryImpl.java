package com.hankki.domain.food.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;                              // ← 여기!
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import com.hankki.domain.food.entity.Food;

import java.util.List;
import java.util.Optional;

@Repository
public class FoodRepositoryImpl implements FoodRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Food> findBestMatchByTokens(List<String> tokens) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        // Tuple 리턴으로 (Food, score) 동시에 꺼내기
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Food> root = cq.from(Food.class);

        // score는 Number 타입으로 선언해야 sum() 반환값과 맞습니다
        Expression<Number> score = cb.literal(0);

        for (String token : tokens) {
            Expression<Integer> match = cb.<Integer>selectCase()
                .when(
                    cb.like(cb.lower(root.get("foodName")),
                            "%" + token.toLowerCase() + "%"),
                    cb.literal(1)   // cb.literal(Integer) 로 명시적 literal
                )
                .otherwise(cb.literal(0));
            score = cb.sum(score, match);
        }
        
        // Food 엔티티와 score 함께 선택
        cq.multiselect(root, score.alias("score"))
          .orderBy(cb.desc(score));

        // 가장 높은 한 건만
        List<Tuple> results = em.createQuery(cq)
                                .setMaxResults(1)
                                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        // Tuple.get(0, Food.class)로 Food 꺼내기
        Food best = results.get(0).get(0, Food.class);
        return Optional.of(best);
    }
}
