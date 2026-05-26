package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySessionRepository extends JpaRepository<DailySession, Long> {
    Optional<DailySession> findBySessionDate(LocalDate date);
}
