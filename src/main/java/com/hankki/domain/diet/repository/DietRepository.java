package com.hankki.domain.diet.repository;

import com.hankki.domain.diet.entity.Diet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietRepository  extends JpaRepository<Diet, Long> {
    void insertDiet(Diet diet);
    Optional<Diet> findByEmail(String email);
    Optional<Diet> findByDietId(Long dietId);
    List<Diet> findDietsByEmailAndTakeAt(String email, LocalDate takeAt);
    void deleteDietById(Long dietId);
}
