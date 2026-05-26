package com.example.teodormihai.demo.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull Long proposalId) {}
