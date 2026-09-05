package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="client_sessions", indexes={@Index(name="idx_session_token", columnList="token", unique=true)})
public class ClientSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Device device;
    @Column(nullable=false, unique=true, length=64) private String token;
    @Column(nullable=false) private LocalDateTime loginAt=LocalDateTime.now();
    @Column(nullable=false) private LocalDateTime lastHeartbeatAt=LocalDateTime.now();
    @Column(nullable=false) private LocalDateTime expiresAt;
    @Column(nullable=false) private boolean revoked=false;
}