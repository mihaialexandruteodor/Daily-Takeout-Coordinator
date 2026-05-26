package com.example.teodormihai.demo.dto;

import com.example.teodormihai.demo.model.ParticipantStatus;

import java.time.LocalDate;
import java.util.List;

public record DailyViewDto(
        Long sessionId,
        LocalDate date,
        String currentUser,
        ParticipantStatus currentUserStatus,
        List<ParticipantDto> participants,
        List<ProposalDto> proposals
) {
    public record ParticipantDto(String userName, ParticipantStatus status) {}
    public record ProposalDto(Long id, String restaurant, long voteCount, boolean votedByCurrentUser) {}
}
