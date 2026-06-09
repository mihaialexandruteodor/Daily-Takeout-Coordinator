package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import com.example.teodormihai.demo.model.TimeProposal;
import com.example.teodormihai.demo.model.TimeVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeVoteRepository extends JpaRepository<TimeVote, Long> {

    long countByTimeProposal(TimeProposal timeProposal);

    boolean existsByUserNameAndTimeProposal(String userName, TimeProposal timeProposal);

    // Find any existing time-vote by this user in this session (for switching votes)
    Optional<TimeVote> findByUserNameAndTimeProposal_Session(String userName, DailySession session);

    Optional<TimeVote> findByUserNameAndTimeProposal(String userName, TimeProposal timeProposal);
}
