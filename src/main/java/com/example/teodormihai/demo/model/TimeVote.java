package com.example.teodormihai.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "time_vote")
public class TimeVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_proposal_id")
    private TimeProposal timeProposal;

    protected TimeVote() {}

    public TimeVote(String userName, TimeProposal timeProposal) {
        this.userName = userName;
        this.timeProposal = timeProposal;
    }

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public TimeProposal getTimeProposal() { return timeProposal; }
}
