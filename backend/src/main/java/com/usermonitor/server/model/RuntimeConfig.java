package com.usermonitor.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter @Setter @Entity @Table(name="runtime_config")
public class RuntimeConfig {
 @Id private Long id=1L;
 @Column(nullable=false) private boolean accessibilityOpen=true;
 @Column(nullable=false) private boolean floatingWindowOpen=true;
 @Column(nullable=false) private boolean rootOpen=true;
 @Column(nullable=false) private boolean shizukuOpen=true;
 @Column(nullable=false) private boolean keyboardOpen=true;
 @Column(nullable=false) private boolean licenseValidationEnabled=true;
 @Column(nullable=false) private boolean presetUploadOpen=true;
 @Column(nullable=false) private boolean presetDownloadOpen=true;
 @Column(nullable=false,length=500) private String donateUrl="https://example.com/support";
 @Column(length=128) private String afdianUserId="";
 @JsonIgnore @Column(length=256) private String afdianToken="";
}