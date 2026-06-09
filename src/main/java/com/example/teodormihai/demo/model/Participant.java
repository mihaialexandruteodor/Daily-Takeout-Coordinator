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

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    protected Participant() {}

    public Participant(String userName, ParticipantStatus status, DailySession session) {
        this.userName = userName;
        this.status = status;
        this.session = session;
    }

    public Participant(String userName, ParticipantStatus status, DailySession session, String ipAddress) {
        this.userName = userName;
        this.status = status;
        this.session = session;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public ParticipantStatus getStatus() { return status; }
    public DailySession getSession() { return session; }
    public String getIpAddress() { return ipAddress; }

    public void setStatus(ParticipantStatus status) { this.status = status; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
