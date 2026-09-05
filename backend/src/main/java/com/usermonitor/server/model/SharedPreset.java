package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="shared_presets")
public class SharedPreset {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=80) private String name;
 @Column(length=500) private String description="";
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="uploader_id",nullable=false) private UserAccount uploader;
 @Lob @Column(nullable=false,columnDefinition="LONGBLOB") private byte[] fileData;
 @Column(nullable=false) private long fileSize;
 @Column(nullable=false) private long downloadCount=0;
 @Column(nullable=false) private boolean published=false;
 @Column(nullable=false,columnDefinition="boolean default true") private boolean publicVisible=true;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
 private LocalDateTime updatedAt;
}