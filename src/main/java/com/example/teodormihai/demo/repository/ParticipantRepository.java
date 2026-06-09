package com.example.teodormihai.demo.repository;

import com.example.teodormihai.demo.model.DailySession;
import com.example.teodormihai.demo.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findBySession(DailySession session);

    @Query("SELECT p FROM Participant p WHERE LOWER(p.userName) = LOWER(:name) AND p.session = :session")
    Optional<Participant> findByUserNameIgnoreCaseAndSession(@Param("name") String name,
                                                             @Param("session") DailySession session);

    Optional<Participant> findByIpAddressAndSession(String ipAddress, DailySession session);
}
