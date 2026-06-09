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
    private final TimeProposalRepository timeProposalRepo;
    private final TimeVoteRepository timeVoteRepo;

    public TakeoutService(DailySessionRepository sessionRepo,
                          ParticipantRepository participantRepo,
                          ProposalRepository proposalRepo,
                          VoteRepository voteRepo,
                          TimeProposalRepository timeProposalRepo,
                          TimeVoteRepository timeVoteRepo) {
        this.sessionRepo = sessionRepo;
        this.participantRepo = participantRepo;
        this.proposalRepo = proposalRepo;
        this.voteRepo = voteRepo;
        this.timeProposalRepo = timeProposalRepo;
        this.timeVoteRepo = timeVoteRepo;
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

    /**
     * Registers a participant for today.
     *
     * IP dedup: if this IP has already joined today, return the existing participant
     * (so the caller can restore the cookie for that session). This handles the case
     * where someone cleared cookies on the same machine.
     *
     * Name dedup: if the same name is already taken by a *different* IP, throw DuplicateNameException.
     */
    public Participant registerParticipant(String name, LocalDate date, String ipAddress) {
        final String trimmedName = name.trim();
        if (trimmedName.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        DailySession session = getOrCreateSession(date);

        // IP-based dedup: same IP already joined today → return existing participant
        if (ipAddress != null && !ipAddress.isBlank()) {
            var existing = participantRepo.findByIpAddressAndSession(ipAddress, session);
            if (existing.isPresent()) {
                return existing.get(); // caller should re-set cookie to existing name
            }
        }

        // Name-based dedup: name already taken (by someone else)
        participantRepo.findByUserNameIgnoreCaseAndSession(trimmedName, session).ifPresent(p -> {
            throw new DuplicateNameException(trimmedName);
        });

        Participant participant = new Participant(trimmedName, null, session, ipAddress);
        return participantRepo.save(participant);
    }

    // Keep original signature for backward compat (no IP)
    public Participant registerParticipant(String name, LocalDate date) {
        return registerParticipant(name, date, null);
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

    // ── Time proposals ────────────────────────────────────────────────────────

    public TimeProposal addTimeProposal(String proposedTime, String proposedBy, Long sessionId) {
        final String trimmed = proposedTime.trim();
        DailySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        timeProposalRepo.findByProposedTimeAndSession(trimmed, session).ifPresent(p -> {
            throw new IllegalArgumentException("Time already proposed today: " + trimmed);
        });
        return timeProposalRepo.save(new TimeProposal(trimmed, proposedBy, session));
    }

    public TimeVote castTimeVote(String userName, Long timeProposalId) {
        TimeProposal timeProposal = timeProposalRepo.findById(timeProposalId)
                .orElseThrow(() -> new IllegalArgumentException("Time proposal not found"));
        DailySession session = timeProposal.getSession();
        // Remove any existing time vote by this user in this session (one vote at a time)
        timeVoteRepo.findByUserNameAndTimeProposal_Session(userName, session).ifPresent(existing -> {
            timeVoteRepo.delete(existing);
            timeVoteRepo.flush();
        });
        return timeVoteRepo.save(new TimeVote(userName, timeProposal));
    }

    public void retractTimeVote(Long timeProposalId, String userName) {
        TimeProposal timeProposal = timeProposalRepo.findById(timeProposalId)
                .orElseThrow(() -> new IllegalArgumentException("Time proposal not found"));
        timeVoteRepo.findByUserNameAndTimeProposal(userName, timeProposal).ifPresent(timeVoteRepo::delete);
    }

    // ── View ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DailyViewDto getDailyView(String userName, LocalDate date) {
        DailySession session = sessionRepo.findBySessionDate(date).orElse(null);
        if (session == null) {
            return new DailyViewDto(null, date, userName, null, List.of(), List.of(), List.of());
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

        List<DailyViewDto.TimeProposalDto> timeProposals =
                timeProposalRepo.findBySessionOrderByProposedTime(session).stream()
                        .map(tp -> new DailyViewDto.TimeProposalDto(
                                tp.getId(),
                                tp.getProposedTime(),
                                tp.getProposedBy(),
                                timeVoteRepo.countByTimeProposal(tp),
                                timeVoteRepo.existsByUserNameAndTimeProposal(userName, tp)
                        ))
                        .toList();

        return new DailyViewDto(session.getId(), date, userName, currentStatus,
                participants, proposals, timeProposals);
    }
}
