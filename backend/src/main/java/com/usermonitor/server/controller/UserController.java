package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.UserAccount;
import com.usermonitor.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
@RestController @RequestMapping("/api/client/users") @RequiredArgsConstructor
public class UserController {
 private final UserService service;
 @PostMapping(value="/register",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<?> register(@RequestParam String username,@RequestParam String password,@RequestParam String confirmPassword,@RequestParam(required=false,defaultValue="")String deviceCode,@RequestParam(required=false,defaultValue="")String deviceName,@RequestPart(required=false)MultipartFile avatar,HttpServletRequest req)throws Exception{return ApiResponse.ok(service.register(username,password,confirmPassword,deviceCode,deviceName,avatar==null?null:avatar.getBytes(),avatar==null?null:avatar.getContentType(),ip(req)));}
 @PostMapping("/login") public ApiResponse<?> login(@RequestBody Map<String,String> r,HttpServletRequest req){return ApiResponse.ok(service.login(r.getOrDefault("login",""),r.getOrDefault("password",""),r.getOrDefault("deviceCode",""),ip(req)));}
 @GetMapping("/me") public ApiResponse<?> me(@RequestHeader("Authorization")String auth){return ApiResponse.ok(service.profile(service.require(bearer(auth))));}
 @PatchMapping("/me/name") public ApiResponse<?> changeName(@RequestHeader("Authorization")String auth,@RequestBody Map<String,String> r){return ApiResponse.ok(service.changeUsername(bearer(auth),r.getOrDefault("username","")));}
 @PatchMapping("/me/password") public ApiResponse<?> changePassword(@RequestHeader("Authorization")String auth,@RequestBody Map<String,String> r){service.changePassword(bearer(auth),r.getOrDefault("oldPassword",""),r.getOrDefault("newPassword",""),r.getOrDefault("confirmPassword",""));return ApiResponse.ok(Map.of("changed",true));}
 @PostMapping(value="/me/avatar",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<?> changeAvatar(@RequestHeader("Authorization")String auth,@RequestPart MultipartFile file)throws Exception{return ApiResponse.ok(service.changeAvatar(bearer(auth),file.getBytes(),file.getContentType()));}
 @GetMapping("/titles") public ApiResponse<?> titles(@RequestHeader("Authorization")String auth){return ApiResponse.ok(service.titleApplications(bearer(auth)));}
 @PostMapping("/titles") public ApiResponse<?> applyTitle(@RequestHeader("Authorization")String auth,@RequestBody Map<String,String> r){return ApiResponse.ok(service.applyTitle(bearer(auth),r.getOrDefault("title",""),r.getOrDefault("reason","")));}
 @GetMapping("/{id}/avatar") public ResponseEntity<byte[]> avatar(@PathVariable String id){UserAccount u=service.find(id);if(u.getAvatar()==null)return ResponseEntity.notFound().build();return ResponseEntity.ok().header("X-Content-Type-Options","nosniff").header("Content-Security-Policy","default-src 'none'; sandbox").contentType(MediaType.parseMediaType(com.usermonitor.server.service.AvatarImages.contentType(u.getAvatar()))).cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7))).body(u.getAvatar());}
 static String bearer(String h){return h!=null&&h.startsWith("Bearer ")?h.substring(7):"";}
 static String ip(HttpServletRequest r){return r.getRemoteAddr();}
}