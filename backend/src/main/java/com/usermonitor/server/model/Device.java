package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="devices", uniqueConstraints=@UniqueConstraint(name="uk_license_device", columnNames={"license_id","device_code"}))
public class Device {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) @JoinColumn(name="license_id") private LicenseKey license;
    @Column(name="device_code", nullable=false, length=128) private String deviceCode;
    @Column(length=100) private String deviceName;
    @Column(nullable=false) private boolean enabled=false;
    @Column(nullable=false, updatable=false) private LocalDateTime boundAt=LocalDateTime.now();
    private LocalDateTime lastSeenAt;
    @Column(length=64) private String lastIp;
}