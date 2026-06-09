package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import com.example.teodormihai.demo.model.TimeProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeProposalRepository extends JpaRepository<TimeProposal, Long> {

    List<TimeProposal> findBySessionOrderByProposedTime(DailySession session);

    Optional<TimeProposal> findByProposedTimeAndSession(String proposedTime, DailySession session);
}
