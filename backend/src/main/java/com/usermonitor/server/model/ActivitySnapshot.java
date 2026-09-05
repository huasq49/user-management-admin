package com.usermonitor.server.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="activity_snapshots",indexes=@Index(name="idx_activity_captured",columnList="capturedAt"))
public class ActivitySnapshot {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private LocalDateTime capturedAt=LocalDateTime.now();
 @Column(nullable=false) private long online;
 @Column(nullable=false) private long activeUsers;
 @Column(nullable=false) private long visits;
}