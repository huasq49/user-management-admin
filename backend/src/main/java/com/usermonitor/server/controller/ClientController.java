package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.dto.Requests;
import com.usermonitor.server.model.RuntimeConfig;
import com.usermonitor.server.repository.*;
import com.usermonitor.server.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/client") @RequiredArgsConstructor
public class ClientController {
 private final LicenseService service;private final AnnouncementRepository announcements;private final AppVersionRepository versions;private final RuntimeConfigRepository runtimeConfigs;
 @PostMapping("/device-status") public ApiResponse<?> deviceStatus(@Valid @RequestBody Requests.DeviceStatus r,HttpServletRequest req){return ApiResponse.ok(Map.of("enabled",service.registerAndCheckDevice(r.deviceCode(),r.deviceName(),ip(req))));}
 @PostMapping("/validate-card") public ApiResponse<?> validate(@Valid @RequestBody Requests.ValidateCard r){return ApiResponse.ok(Map.of("valid",service.validateCard(r.licenseKey(),r.deviceCode())));}
 @PostMapping("/login") public ApiResponse<Map<String,Object>> login(@Valid @RequestBody Requests.ClientLogin r,HttpServletRequest req){return ApiResponse.ok(service.clientLogin(r,ip(req)));}
 @PostMapping("/heartbeat") public ApiResponse<Map<String,Object>> heartbeat(@Valid @RequestBody Requests.Heartbeat r,HttpServletRequest req){return ApiResponse.ok(service.heartbeat(r.token(),ip(req)));}
 @GetMapping("/announcements") public ApiResponse<?> announcements(){return ApiResponse.ok(announcements.findByPublishedTrueOrderByPinnedDescCreatedAtDesc());}
 @GetMapping("/preset-api-status") public ApiResponse<?> presetApiStatus(){RuntimeConfig c=runtimeConfigs.findById(1L).orElseGet(()->runtimeConfigs.save(new RuntimeConfig()));return ApiResponse.ok(Map.of("uploadOpen",c.isPresetUploadOpen(),"downloadOpen",c.isPresetDownloadOpen()));}
 @GetMapping("/runtime-config") public ApiResponse<?> runtimeConfig(){return ApiResponse.ok(runtimeConfigs.findById(1L).orElseGet(()->runtimeConfigs.save(new RuntimeConfig())));}
 // 始终返回服务器当前发布版本。客户端必须比较 versionCode 是否完全一致；不一致即强制更新。
 @GetMapping("/version") public ApiResponse<?> version(@RequestParam String appId,@RequestParam long versionCode){return ApiResponse.ok(versions.findFirstByAppIdAndPublishedTrueOrderByVersionCodeDesc(appId).orElse(null));}
 private String ip(HttpServletRequest r){return r.getRemoteAddr();}
}