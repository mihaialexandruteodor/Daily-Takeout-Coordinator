package com.example.teodormihai.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ProposalRequest(@NotBlank String restaurant) {}
