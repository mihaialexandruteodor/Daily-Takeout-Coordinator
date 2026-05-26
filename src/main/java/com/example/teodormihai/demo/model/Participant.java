package com.example.teodormihai.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participant")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ParticipantStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private DailySession session;

    protected Participant() {}

    public Participant(String userName, ParticipantStatus status, DailySession session) {
        this.userName = userName;
        this.status = status;
        this.session = session;
    }

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public ParticipantStatus getStatus() { return status; }
    public DailySession getSession() { return session; }

    public void setStatus(ParticipantStatus status) { this.status = status; }
}
