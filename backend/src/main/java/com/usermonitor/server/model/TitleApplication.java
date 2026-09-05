package com.usermonitor.server.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="title_applications")
public class TitleApplication {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="user_id") private UserAccount user;
 @Column(nullable=false,length=32) private String title;
 @Column(nullable=false,length=500) private String reason="";
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private Status status=Status.PENDING;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
 private LocalDateTime reviewedAt;
 public enum Status { PENDING, APPROVED, REJECTED }
}