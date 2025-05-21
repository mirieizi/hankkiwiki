package com.hankki.domain.recommend.repository;

import com.hankki.domain.recommend.entity.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    Optional<UserLog> findByUserIdAndDate(Long userId, LocalDate date);
}
