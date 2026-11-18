package com.practice.nexora.advisor;


import com.practice.nexora.user.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "advisor_messages")
public class AdvisorMessage {

    static final int MAX_CONTENT_LENGTH = 4000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "user_id", nullable = false)
    private ChatRole role;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdvisorMessage() {

    }

    public AdvisorMessage(UserAccount user, ChatRole role, String content) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        String text = content == null ? "" : content;
        this.content = text.length() > MAX_CONTENT_LENGTH ? text.substring(0, MAX_CONTENT_LENGTH) : text;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
    public ChatRole getRole() {
        return role;
    }
    public String getContent() {
        return content;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
