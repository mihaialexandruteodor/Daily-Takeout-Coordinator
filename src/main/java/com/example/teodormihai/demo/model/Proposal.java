package com.example.teodormihai.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proposal")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private DailySession session;

    protected Proposal() {}

    public Proposal(String restaurant, DailySession session) {
        this.restaurant = restaurant;
        this.session = session;
    }

    public Long getId() { return id; }
    public String getRestaurant() { return restaurant; }
    public DailySession getSession() { return session; }
}
