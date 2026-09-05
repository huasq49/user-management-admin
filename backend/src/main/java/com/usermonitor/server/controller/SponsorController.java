package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.service.AfdianService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/client") @RequiredArgsConstructor
public class SponsorController {
 private final AfdianService afdian;
 @GetMapping("/sponsors") public ApiResponse<?> sponsors() throws Exception{return ApiResponse.ok(afdian.sponsors());}
}