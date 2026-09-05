package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.service.ActivityService;
import com.usermonitor.server.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Backward-compatible dashboard routes used by older admin clients. */
@RestController
@RequiredArgsConstructor
public class LegacyDashboardController {
    private final LicenseService licenses;
    private final ActivityService activity;

    @GetMapping("/api/overview")
    public ApiResponse<?> overview() { return ApiResponse.ok(licenses.stats()); }

    @GetMapping("/api/dashboard")
    public ApiResponse<?> dashboard(@RequestParam(defaultValue="24") int hours) {
        return ApiResponse.ok(activity.history(hours));
    }
}
