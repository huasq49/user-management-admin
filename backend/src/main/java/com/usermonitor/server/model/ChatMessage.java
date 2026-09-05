package com.usermonitor.server.model;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity
@Table(name="chat_messages", indexes={@Index(name="idx_chat_created",columnList="createdAt"),@Index(name="idx_chat_user",columnList="user_id")})
public class ChatMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="user_id") private UserAccount user;
 @Column(nullable=false,length=500) private String content;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(nullable=false) private boolean pinned=false;
}