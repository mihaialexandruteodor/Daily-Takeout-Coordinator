package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import com.example.teodormihai.demo.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findBySession(DailySession session);

    @Query("SELECT p FROM Proposal p WHERE LOWER(p.restaurant) = LOWER(:restaurant) AND p.session = :session")
    Optional<Proposal> findByRestaurantIgnoreCaseAndSession(@Param("restaurant") String restaurant,
                                                             @Param("session") DailySession session);
}
