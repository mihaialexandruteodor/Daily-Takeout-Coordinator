package com.example.teodormihai.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_session",
       uniqueConstraints = @UniqueConstraint(columnNames = "session_date"))
public class DailySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    protected DailySession() {}

    public DailySession(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Long getId() { return id; }
    public LocalDate getSessionDate() { return sessionDate; }
}
