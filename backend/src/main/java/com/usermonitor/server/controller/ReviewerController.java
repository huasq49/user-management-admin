package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.SharedPreset;
import com.usermonitor.server.repository.SharedPresetRepository;
import com.usermonitor.server.service.ReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/reviewer") @RequiredArgsConstructor
public class ReviewerController {
 private final ReviewerService reviewers; private final SharedPresetRepository presets; private final com.usermonitor.server.service.PresetPreviewService previews;
 @PostMapping("/login") public ApiResponse<?> login(@RequestBody Map<String,String> r){return ApiResponse.ok(Map.of("token",reviewers.login(r.get("username"),r.get("password"))));}
 @GetMapping("/presets/{id}/preview") @org.springframework.transaction.annotation.Transactional(readOnly=true) public ApiResponse<?> preview(@RequestHeader(value="Authorization",required=false)String h,@PathVariable long id){reviewers.require(h);return ApiResponse.ok(previews.preview(get(id).getFileData()));}
 @GetMapping("/presets") @org.springframework.transaction.annotation.Transactional(readOnly=true) public ApiResponse<?> list(@RequestHeader("Authorization")String h){reviewers.require(h);return ApiResponse.ok(presets.findAllByOrderByIdDesc().stream().map(this::dto).toList());}
 @GetMapping("/presets/{id}/download") @org.springframework.transaction.annotation.Transactional(readOnly=true) public ResponseEntity<byte[]> download(@RequestHeader("Authorization")String h,@PathVariable long id){reviewers.require(h);SharedPreset p=get(id);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=review-"+id+".mnd").contentType(MediaType.APPLICATION_OCTET_STREAM).body(p.getFileData());}
 @PostMapping("/presets/{id}/decision") @org.springframework.transaction.annotation.Transactional public ApiResponse<?> decision(@RequestHeader("Authorization")String h,@PathVariable long id,@RequestBody Map<String,Object> r){reviewers.require(h);SharedPreset p=get(id);boolean approved=Boolean.parseBoolean(Objects.toString(r.get("approved"),"false"));p.setPublished(approved);if(r.containsKey("name")){String n=Objects.toString(r.get("name"),"").trim();if(!n.isBlank())p.setName(n);}if(r.containsKey("description"))p.setDescription(Objects.toString(r.get("description"),""));p.setUpdatedAt(LocalDateTime.now());return ApiResponse.ok(dto(presets.save(p)));}
 private SharedPreset get(long id){return presets.findWithUploaderById(id).orElseThrow(()->new IllegalArgumentException("预设不存在"));}
 private Map<String,Object> dto(SharedPreset p){Map<String,Object>m=new LinkedHashMap<>();m.put("id",p.getId());m.put("name",p.getName());m.put("description",Objects.toString(p.getDescription(),""));m.put("uploaderName",p.getUploader().getUsername());m.put("uploaderId",p.getUploader().getAccountId());m.put("fileSize",p.getFileSize());m.put("downloadCount",p.getDownloadCount());m.put("published",p.isPublished());m.put("createdAt",p.getCreatedAt());return m;}
}