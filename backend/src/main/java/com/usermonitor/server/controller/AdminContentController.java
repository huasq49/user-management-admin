package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.dto.Requests;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminContentController {
 private final AnnouncementRepository announcements;private final AppVersionRepository versions;private final RuntimeConfigRepository runtimeConfigs;
 @GetMapping("/announcements") public ApiResponse<?> announcements(){return ApiResponse.ok(announcements.findAll(Sort.by(Sort.Direction.DESC,"id")));}
 @PostMapping("/announcements") public ApiResponse<?> saveAnnouncement(@Valid @RequestBody Requests.AnnouncementSave r){Announcement a=new Announcement();a.setTitle(r.title());a.setContent(r.content());a.setPublished(r.published());a.setPinned(r.pinned());return ApiResponse.ok(announcements.save(a));}
 @PutMapping("/announcements/{id}") public ApiResponse<?> updateAnnouncement(@PathVariable long id,@Valid @RequestBody Requests.AnnouncementSave r){Announcement a=announcements.findById(id).orElseThrow(()->new IllegalArgumentException("公告不存在"));a.setTitle(r.title());a.setContent(r.content());a.setPublished(r.published());a.setPinned(r.pinned());a.setUpdatedAt(LocalDateTime.now());return ApiResponse.ok(announcements.save(a));}
 @DeleteMapping("/announcements/{id}") public ApiResponse<Void> deleteAnnouncement(@PathVariable long id){announcements.deleteById(id);return ApiResponse.ok(null);}
 @GetMapping("/runtime-config") public ApiResponse<?> runtimeConfig(){return ApiResponse.ok(runtimeConfigs.findById(1L).orElseGet(()->runtimeConfigs.save(new RuntimeConfig())));}
 @PutMapping("/runtime-config") public ApiResponse<?> updateRuntimeConfig(@Valid @RequestBody Requests.RuntimeConfigSave r){RuntimeConfig c=runtimeConfigs.findById(1L).orElseGet(RuntimeConfig::new);if(r.accessibilityOpen()!=null)c.setAccessibilityOpen(r.accessibilityOpen());if(r.floatingWindowOpen()!=null)c.setFloatingWindowOpen(r.floatingWindowOpen());if(r.rootOpen()!=null)c.setRootOpen(r.rootOpen());if(r.shizukuOpen()!=null)c.setShizukuOpen(r.shizukuOpen());if(r.keyboardOpen()!=null)c.setKeyboardOpen(r.keyboardOpen());if(r.licenseValidationEnabled()!=null)c.setLicenseValidationEnabled(r.licenseValidationEnabled());if(r.presetUploadOpen()!=null)c.setPresetUploadOpen(r.presetUploadOpen());if(r.presetDownloadOpen()!=null)c.setPresetDownloadOpen(r.presetDownloadOpen());if(r.donateUrl()!=null&&!r.donateUrl().isBlank())c.setDonateUrl(r.donateUrl().trim());if(r.afdianUserId()!=null)c.setAfdianUserId(r.afdianUserId().trim());if(r.afdianToken()!=null&&!r.afdianToken().isBlank())c.setAfdianToken(r.afdianToken().trim());return ApiResponse.ok(runtimeConfigs.save(c));}
 @GetMapping("/versions") public ApiResponse<?> versions(){return ApiResponse.ok(versions.findAll(Sort.by(Sort.Direction.DESC,"id")));}
 @PostMapping("/versions") public ApiResponse<?> saveVersion(@Valid @RequestBody Requests.VersionSave r){AppVersion v=new AppVersion();applyVersion(v,r);return ApiResponse.ok(versions.save(v));}
 @PutMapping("/versions/{id}") public ApiResponse<?> updateVersion(@PathVariable long id,@Valid @RequestBody Requests.VersionSave r){AppVersion v=versions.findById(id).orElseThrow(()->new IllegalArgumentException("版本不存在"));applyVersion(v,r);return ApiResponse.ok(versions.save(v));}
 private void applyVersion(AppVersion v,Requests.VersionSave r){v.setAppId(r.appId());v.setVersionCode(r.versionCode());v.setVersionName(r.versionName());v.setDownloadUrl(r.downloadUrl());v.setChangelog(r.changelog());v.setForceUpdate(r.forceUpdate());v.setPublished(r.published());}
 @DeleteMapping("/versions/{id}") public ApiResponse<Void> deleteVersion(@PathVariable long id){versions.deleteById(id);return ApiResponse.ok(null);}
}