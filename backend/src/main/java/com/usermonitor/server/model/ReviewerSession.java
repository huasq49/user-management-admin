package com.usermonitor.server.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="reviewer_sessions")
public class ReviewerSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) private ReviewerAccount reviewer;
 @Column(nullable=false,unique=true,length=64) private String token;
 @Column(nullable=false) private LocalDateTime expiresAt;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
}