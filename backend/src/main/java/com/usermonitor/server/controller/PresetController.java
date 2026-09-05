package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.SharedPresetRepository;
import com.usermonitor.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/client/presets") @RequiredArgsConstructor
public class PresetController {
 private final SharedPresetRepository presets; private final UserService users; private final com.usermonitor.server.repository.RuntimeConfigRepository runtimeConfigs;
 private com.usermonitor.server.model.RuntimeConfig config(){return runtimeConfigs.findById(1L).orElseGet(()->runtimeConfigs.save(new com.usermonitor.server.model.RuntimeConfig()));}
 private void requireUploadOpen(){if(!config().isPresetUploadOpen())throw new IllegalArgumentException("在线预设上传功能已由管理员关闭");}
 private void requireDownloadOpen(){if(!config().isPresetDownloadOpen())throw new IllegalArgumentException("在线预设下载功能已由管理员关闭");}
 @GetMapping public ApiResponse<?> list(){return ApiResponse.ok(presets.findByPublishedTrueAndPublicVisibleTrueOrderByCreatedAtDesc().stream().map(this::dto).toList());}
 @GetMapping("/mine") @Transactional(readOnly=true) public ApiResponse<?> mine(@RequestHeader("Authorization") String auth){UserAccount u=users.require(UserController.bearer(auth));return ApiResponse.ok(presets.findByUploaderIdOrderByCreatedAtDesc(u.getId()).stream().map(this::dto).toList());}
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<?> upload(@RequestHeader("Authorization")String auth,@RequestParam String name,@RequestParam(required=false,defaultValue="")String description,@RequestParam(required=false,defaultValue="true")boolean publicVisible,@RequestPart MultipartFile file)throws Exception{requireUploadOpen();UserAccount u=users.require(UserController.bearer(auth));if(name.isBlank()||name.length()>80)throw new IllegalArgumentException("预设名称不能为空且不能超过80字");byte[] data=file.getBytes();if(data.length==0||data.length>2*1024*1024)throw new IllegalArgumentException("MND预设大小需在1B-2MB之间");String fn=Objects.toString(file.getOriginalFilename(),"");if(!fn.toLowerCase().endsWith(".mnd"))throw new IllegalArgumentException("只能上传mnd预设");SharedPreset p=new SharedPreset();p.setName(name.trim());p.setDescription(description.trim());p.setUploader(u);p.setFileData(data);p.setFileSize(data.length);p.setPublicVisible(publicVisible);return ApiResponse.ok(dto(presets.save(p)));}
 @PatchMapping("/mine/{id}/visibility") @Transactional public ApiResponse<?> visibility(@RequestHeader("Authorization")String auth,@PathVariable long id,@RequestParam boolean publicVisible){UserAccount u=users.require(UserController.bearer(auth));SharedPreset p=presets.findWithUploaderById(id).filter(x->x.getUploader().getId().equals(u.getId())).orElseThrow(()->new IllegalArgumentException("预设不存在或不属于当前账号"));p.setPublicVisible(publicVisible);p.setUpdatedAt(LocalDateTime.now());return ApiResponse.ok(dto(presets.save(p)));}
 @GetMapping("/mine/{id}/download") @Transactional(readOnly=true) public ResponseEntity<byte[]> mineDownload(@RequestHeader("Authorization")String auth,@PathVariable long id){requireDownloadOpen();UserAccount u=users.require(UserController.bearer(auth));SharedPreset p=presets.findWithUploaderById(id).filter(x->x.getUploader().getId().equals(u.getId())).orElseThrow(()->new IllegalArgumentException("预设不存在或不属于当前账号"));return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"preset-"+id+".mnd\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(p.getFileData());}
 @GetMapping("/{id:\\d+}") public ApiResponse<?> preview(@PathVariable long id){
  SharedPreset p=presets.findById(id).filter(x->x.isPublished()&&x.isPublicVisible()).orElseThrow(()->new IllegalArgumentException("预设不存在"));
  return ApiResponse.ok(dto(p));
 }
 @GetMapping("/{id:\\d+}/download") @Transactional public ResponseEntity<byte[]> download(@PathVariable long id){requireDownloadOpen();SharedPreset p=presets.findById(id).filter(x->x.isPublished()&&x.isPublicVisible()).orElseThrow(()->new IllegalArgumentException("预设不存在"));p.setDownloadCount(p.getDownloadCount()+1);presets.save(p);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"preset-"+id+".mnd\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(p.getFileData());}
 private Map<String,Object> dto(SharedPreset p){Map<String,Object> m=new LinkedHashMap<>();m.put("id",p.getId());m.put("name",p.getName());m.put("description",Objects.toString(p.getDescription(),""));m.put("uploader",users.profile(p.getUploader()));m.put("fileSize",p.getFileSize());m.put("downloadCount",p.getDownloadCount());m.put("published",p.isPublished());m.put("publicVisible",p.isPublicVisible());m.put("createdAt",p.getCreatedAt());m.put("updatedAt",p.getUpdatedAt());return m;}
}