package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="announcements")
public class Announcement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=120) private String title;
    @Lob @Column(nullable=false) private String content;
    @Column(nullable=false) private boolean published=true;
    @Column(nullable=false) private boolean pinned=false;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime updatedAt=LocalDateTime.now();
}