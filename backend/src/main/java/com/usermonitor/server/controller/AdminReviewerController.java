package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.service.ReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/reviewers") @RequiredArgsConstructor
public class AdminReviewerController {
 private final ReviewerService service;
 @GetMapping public ApiResponse<?> list(){return ApiResponse.ok(service.list());}
 @PostMapping("/generate") public ApiResponse<?> generate(@RequestParam(defaultValue="1")int count){return ApiResponse.ok(service.generate(count));}
 @PatchMapping("/{id}/enabled") public ApiResponse<Void> enabled(@PathVariable long id,@RequestParam boolean enabled){service.enabled(id,enabled);return ApiResponse.ok(null);}
 @PostMapping("/{id}/reset-password") public ApiResponse<?> resetPassword(@PathVariable long id){return ApiResponse.ok(service.resetPassword(id));}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id){service.delete(id);return ApiResponse.ok(null);}
}