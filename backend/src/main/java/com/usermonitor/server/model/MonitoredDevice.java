package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="monitored_devices",indexes={@Index(name="idx_monitor_device_code",columnList="deviceCode",unique=true)})
public class MonitoredDevice {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=128) private String deviceCode;
 @Column(length=100) private String deviceName;
 @Column(length=64) private String lastIp;
 @Column(nullable=false) private boolean enabled=false;
 @Column(nullable=false) private long visitCount=0;
 @Column(nullable=false,updatable=false) private LocalDateTime firstSeenAt=LocalDateTime.now();
 @Column(nullable=false) private LocalDateTime lastSeenAt=LocalDateTime.now();
}