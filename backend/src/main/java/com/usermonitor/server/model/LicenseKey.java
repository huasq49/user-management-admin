package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="license_keys", indexes={@Index(name="idx_license_code", columnList="code", unique=true)})
public class LicenseKey {
    public enum Status { UNUSED, ACTIVE, DISABLED, EXPIRED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=64) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Status status=Status.UNUSED;
    @Column(nullable=false) private Integer durationDays=30;
    @Column(nullable=false) private Integer maxDevices=1;
    /** 0=不限次数；大于0=可验证次数。单码卡密通常设为1。 */
    @Column(nullable=false) private Integer maxUses=0;
    @Column(nullable=false) private Integer usedCount=0;
    @Column(nullable=false) private boolean deviceBindingEnabled=true;
    @Column(nullable=false) private boolean deviceVerificationEnabled=true;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    @Column(length=255) private String remark;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt=LocalDateTime.now();
}