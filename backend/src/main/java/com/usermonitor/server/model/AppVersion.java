package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="app_versions")
public class AppVersion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=50) private String appId;
    @Column(nullable=false) private long versionCode;
    @Column(nullable=false, length=50) private String versionName;
    @Column(nullable=false, length=500) private String downloadUrl;
    @Lob private String changelog;
    @Column(nullable=false) private boolean forceUpdate=false;
    @Column(nullable=false) private boolean published=true;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}