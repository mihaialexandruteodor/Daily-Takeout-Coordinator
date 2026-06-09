package com.example.teodormihai.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "time_proposal")
public class TimeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String proposedTime; // e.g. "12:30"

    @Column(nullable = false)
    private String proposedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private DailySession session;

    protected TimeProposal() {}

    public TimeProposal(String proposedTime, String proposedBy, DailySession session) {
        this.proposedTime = proposedTime;
        this.proposedBy = proposedBy;
        this.session = session;
    }

    public Long getId() { return id; }
    public String getProposedTime() { return proposedTime; }
    public String getProposedBy() { return proposedBy; }
    public DailySession getSession() { return session; }
}
