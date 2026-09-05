package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.*;
import com.usermonitor.server.service.UserService;
import com.usermonitor.server.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminUserPresetController {
 private final UserAccountRepository accounts;private final UserSessionRepository sessions;private final SharedPresetRepository presets;private final UserService users;private final AdminUserService adminUsers; private final com.usermonitor.server.service.PresetPreviewService previews;
 @GetMapping("/users") public ApiResponse<?> userList(@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="")String deviceCode){List<UserAccount> list;if(!deviceCode.isBlank())list=accounts.findByDeviceCodeContainingIgnoreCase(deviceCode.trim());else if(!keyword.isBlank())list=accounts.findByUsernameContainingIgnoreCaseOrAccountIdContainingIgnoreCase(keyword.trim(),keyword.trim());else list=accounts.findAll(Sort.by(Sort.Direction.DESC,"id"));return ApiResponse.ok(list.stream().map(this::userDto).toList());}
 @PatchMapping("/users/{id}/enabled") public ApiResponse<?> userEnabled(@PathVariable long id,@RequestParam boolean enabled){UserAccount u=adminUsers.update(id,Map.of("enabled",enabled));return ApiResponse.ok(userDto(u));}
 @PutMapping("/users/{id}") public ApiResponse<?> updateUser(@PathVariable long id,@RequestBody Map<String,Object> r){r.remove("deviceCode");r.remove("deviceName");return ApiResponse.ok(userDto(adminUsers.update(id,r)));}
 @PostMapping("/users/{id}/reset-password") public ApiResponse<?> resetUserPassword(@PathVariable long id){return ApiResponse.ok(adminUsers.resetPassword(id));}
 @PostMapping("/users/{id}/logout") public ApiResponse<Void> logoutUser(@PathVariable long id){adminUsers.logout(id);return ApiResponse.ok(null);}
 @DeleteMapping("/users/{id}") public ApiResponse<Void> deleteUser(@PathVariable long id){adminUsers.delete(id);return ApiResponse.ok(null);}
 @GetMapping("/presets/{id}/preview") @org.springframework.transaction.annotation.Transactional(readOnly=true) public ApiResponse<?> preview(@PathVariable long id){return ApiResponse.ok(previews.preview(presets.findById(id).orElseThrow(()->new IllegalArgumentException("预设不存在")).getFileData()));}
 @GetMapping("/presets") @org.springframework.transaction.annotation.Transactional(readOnly=true) public ApiResponse<?> presetList(){return ApiResponse.ok(presets.findAllByOrderByIdDesc().stream().map(this::dto).toList());}
 @PutMapping("/presets/{id}") public ApiResponse<?> edit(@PathVariable long id,@RequestBody Map<String,Object> r){SharedPreset p=presets.findWithUploaderById(id).orElseThrow(()->new IllegalArgumentException("预设不存在"));if(r.containsKey("name"))p.setName(Objects.toString(r.get("name"),"").trim());if(r.containsKey("description"))p.setDescription(Objects.toString(r.get("description"),""));if(r.containsKey("published"))p.setPublished(Boolean.parseBoolean(Objects.toString(r.get("published"))));p.setUpdatedAt(LocalDateTime.now());presets.save(p);return ApiResponse.ok(dto(presets.findWithUploaderById(p.getId()).orElseThrow(() -> new IllegalArgumentException("预设不存在"))));}
 @DeleteMapping("/presets/{id}") public ApiResponse<Void> delete(@PathVariable long id){presets.deleteById(id);return ApiResponse.ok(null);}
 private Map<String,Object>userDto(UserAccount u){Map<String,Object>m=new LinkedHashMap<>(users.profile(u));m.put("ipAddress",Objects.toString(u.getLastIp(),""));m.put("enabled",u.isEnabled());m.put("online",sessions.existsByUserIdAndExpiresAtAfter(u.getId(),LocalDateTime.now()));m.put("lastLoginAt",u.getLastLoginAt());return m;}
 private Map<String,Object>dto(SharedPreset p){return Map.of("id",p.getId(),"name",p.getName(),"description",Objects.toString(p.getDescription(),""),"uploaderName",p.getUploader().getUsername(),"uploaderId",p.getUploader().getAccountId(),"fileSize",p.getFileSize(),"downloadCount",p.getDownloadCount(),"published",p.isPublished(),"createdAt",p.getCreatedAt());}
}