package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="reviewer_accounts")
public class ReviewerAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=32) private String username;
 @Column(nullable=false,length=100) private String passwordHash;
 @Column(nullable=false) private boolean enabled=true;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
 private LocalDateTime lastLoginAt;
}