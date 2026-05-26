package com.example.teodormihai.demo.service;

import com.example.teodormihai.demo.dto.DailyViewDto;
import com.example.teodormihai.demo.model.*;
import com.example.teodormihai.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TakeoutService {

    private final DailySessionRepository sessionRepo;
    private final ParticipantRepository participantRepo;
    private final ProposalRepository proposalRepo;
    private final VoteRepository voteRepo;

    public TakeoutService(DailySessionRepository sessionRepo,
                          ParticipantRepository participantRepo,
                          ProposalRepository proposalRepo,
                          VoteRepository voteRepo) {
        this.sessionRepo = sessionRepo;
        this.participantRepo = participantRepo;
        this.proposalRepo = proposalRepo;
        this.voteRepo = voteRepo;
    }

    public DailySession getOrCreateSession(LocalDate date) {
        return sessionRepo.findBySessionDate(date).orElseGet(() -> {
            try {
                return sessionRepo.saveAndFlush(new DailySession(date));
            } catch (RuntimeException e) {
                return sessionRepo.findBySessionDate(date).orElseThrow();
            }
        });
    }

    public Participant registerParticipant(String name, LocalDate date) {
        final String trimmedName = name.trim();
        if (trimmedName.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        DailySession session = getOrCreateSession(date);
        participantRepo.findByUserNameIgnoreCaseAndSession(trimmedName, session).ifPresent(p -> {
            throw new DuplicateNameException(trimmedName);
        });
        return participantRepo.save(new Participant(trimmedName, null, session));
    }

    public Participant setStatus(String userName, Long sessionId, ParticipantStatus status) {
        DailySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Participant participant = participantRepo.findByUserNameIgnoreCaseAndSession(userName, session)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        participant.setStatus(status);
        return participantRepo.save(participant);
    }

    public Proposal addProposal(String restaurant, Long sessionId) {
        final String trimmedRestaurant = restaurant.trim();
        if (trimmedRestaurant.isBlank()) {
            throw new IllegalArgumentException("Restaurant name must not be blank");
        }
        DailySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        proposalRepo.findByRestaurantIgnoreCaseAndSession(trimmedRestaurant, session).ifPresent(p -> {
            throw new IllegalArgumentException("Restaurant already proposed today: " + trimmedRestaurant);
        });
        return proposalRepo.save(new Proposal(trimmedRestaurant, session));
    }

    public Vote castVote(String userName, Long proposalId) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        DailySession session = proposal.getSession();
        voteRepo.findByUserNameAndSession(userName, session).ifPresent(existing -> {
            voteRepo.delete(existing);
            voteRepo.flush();
        });
        return voteRepo.save(new Vote(userName, proposal));
    }

    public void retractVote(Long proposalId, String userName) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        voteRepo.findByUserNameAndProposal(userName, proposal).ifPresent(voteRepo::delete);
    }

    @Transactional(readOnly = true)
    public DailyViewDto getDailyView(String userName, LocalDate date) {
        DailySession session = sessionRepo.findBySessionDate(date).orElse(null);
        if (session == null) {
            return new DailyViewDto(null, date, userName, null, List.of(), List.of());
        }

        Participant currentParticipant = participantRepo
                .findByUserNameIgnoreCaseAndSession(userName, session)
                .orElse(null);
        ParticipantStatus currentStatus = currentParticipant != null ? currentParticipant.getStatus() : null;

        List<DailyViewDto.ParticipantDto> participants = participantRepo.findBySession(session).stream()
                .map(p -> new DailyViewDto.ParticipantDto(p.getUserName(), p.getStatus()))
                .toList();

        List<DailyViewDto.ProposalDto> proposals = proposalRepo.findBySession(session).stream()
                .map(p -> new DailyViewDto.ProposalDto(
                        p.getId(),
                        p.getRestaurant(),
                        voteRepo.countByProposal(p),
                        voteRepo.existsByUserNameAndProposal(userName, p)
                ))
                .toList();

        return new DailyViewDto(session.getId(), date, userName, currentStatus, participants, proposals);
    }
}
