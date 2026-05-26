package com.example.teodormihai.demo.dto;

import com.example.teodormihai.demo.model.ParticipantStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull ParticipantStatus status) {}
