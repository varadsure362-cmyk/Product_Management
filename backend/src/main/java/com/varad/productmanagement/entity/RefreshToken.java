package com.varad.productmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry", nullable = false)
    private Instant expiry;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @PrePersist
    protected void onCreate() {
        this.createdOn = Instant.now();
    }
}
