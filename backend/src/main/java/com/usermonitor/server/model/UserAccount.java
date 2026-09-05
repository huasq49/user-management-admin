package com.usermonitor.server.model;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="user_accounts")
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=32) private String accountId;
 @Column(nullable=false,unique=true,length=40) private String username;
 @com.fasterxml.jackson.annotation.JsonIgnore
 @Column(nullable=false,length=100) private String passwordHash;
 @Column(length=128) private String deviceCode;
 @Column(length=100) private String deviceName;
 @Column(length=64) private String lastIp;
 @com.fasterxml.jackson.annotation.JsonIgnore
 @Lob @Basic(fetch=FetchType.LAZY) private byte[] avatar;
 @Column(length=40) private String avatarContentType;
 @Column(nullable=false) private boolean enabled=true;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
 private LocalDateTime lastLoginAt;
 @Column(nullable=false) private boolean chatBanned=false;
 private LocalDateTime chatMutedUntil;
 @Column(nullable=false,length=20) private String systemRole="USER";
 @Column(length=32) private String customTitle;
 @Column(nullable=false) private boolean doNotDisturb=false;
}