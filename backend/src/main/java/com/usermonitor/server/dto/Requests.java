package com.usermonitor.server.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class Requests {
 private Requests(){}
 public record AdminLogin(@NotBlank String username,@NotBlank String password){}
 public record GenerateKeys(@Min(1) @Max(1000) int count,@Min(1) int durationDays,@Min(1) @Max(20) int maxDevices,@Min(0) Integer maxUses,Boolean deviceBindingEnabled,Boolean deviceVerificationEnabled,String prefix,String remark,@Size(max=64) String customCode){}
 public record LicenseUpdate(@Min(1) Integer durationDays,@Min(1) @Max(20) Integer maxDevices,@Min(0) Integer maxUses,Boolean deviceBindingEnabled,Boolean deviceVerificationEnabled,LocalDateTime expiresAt,String remark){}
 public record DeviceRebind(@NotBlank @Size(max=128) String deviceCode,@Size(max=100) String deviceName){}
 public record ClientLogin(@NotBlank String licenseKey,@NotBlank @Size(max=128) String deviceCode,@Size(max=100) String deviceName){}
 public record Heartbeat(@NotBlank String token){}
 public record ValidateCard(@NotBlank String licenseKey,@NotBlank @Size(max=128) String deviceCode,@Size(max=100) String deviceName){}
 public record DeviceStatus(@NotBlank @Size(max=128) String deviceCode,@Size(max=100) String deviceName){}
 public record AnnouncementSave(@NotBlank @Size(max=120) String title,@NotBlank String content,boolean published,boolean pinned){}
 public record VersionSave(@NotBlank String appId,@Min(1) long versionCode,@NotBlank String versionName,@NotBlank String downloadUrl,String changelog,boolean forceUpdate,boolean published){}
 public record RuntimeConfigSave(Boolean accessibilityOpen,Boolean floatingWindowOpen,Boolean rootOpen,Boolean shizukuOpen,Boolean keyboardOpen,Boolean licenseValidationEnabled,Boolean presetUploadOpen,Boolean presetDownloadOpen,@Size(max=500) String donateUrl,@Size(max=128) String afdianUserId,@Size(max=256) String afdianToken){}
}