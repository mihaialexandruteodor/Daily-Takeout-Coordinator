package com.example.teodormihai.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vote")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id")
    private Proposal proposal;

    protected Vote() {}

    public Vote(String userName, Proposal proposal) {
        this.userName = userName;
        this.proposal = proposal;
    }

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public Proposal getProposal() { return proposal; }
}
