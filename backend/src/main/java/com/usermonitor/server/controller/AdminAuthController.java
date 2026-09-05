package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.dto.Requests;
import com.usermonitor.server.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminAuthController {
 private final AdminAuthService auth;
 @PostMapping("/login") public ApiResponse<Map<String,String>> login(@Valid @RequestBody Requests.AdminLogin r){return ApiResponse.ok(Map.of("token",auth.login(r.username(),r.password())));}
 @PostMapping("/logout") public ApiResponse<Void> logout(@RequestHeader("Authorization")String h){auth.logout(h.substring(7));return ApiResponse.ok(null);}
}