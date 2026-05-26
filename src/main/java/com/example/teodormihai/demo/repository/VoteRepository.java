package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import com.example.teodormihai.demo.model.Proposal;
import com.example.teodormihai.demo.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v WHERE v.userName = :userName AND v.proposal.session = :session")
    Optional<Vote> findByUserNameAndSession(@Param("userName") String userName,
                                             @Param("session") DailySession session);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.proposal = :proposal")
    long countByProposal(@Param("proposal") Proposal proposal);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN TRUE ELSE FALSE END FROM Vote v WHERE v.userName = :userName AND v.proposal = :proposal")
    boolean existsByUserNameAndProposal(@Param("userName") String userName,
                                         @Param("proposal") Proposal proposal);

    @Query("SELECT v FROM Vote v WHERE v.userName = :userName AND v.proposal = :proposal")
    Optional<Vote> findByUserNameAndProposal(@Param("userName") String userName,
                                              @Param("proposal") Proposal proposal);
}
