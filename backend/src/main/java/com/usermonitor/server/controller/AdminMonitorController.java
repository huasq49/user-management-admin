package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.repository.MonitoredDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/monitored-devices") @RequiredArgsConstructor
public class AdminMonitorController {
 private final MonitoredDeviceRepository devices;
 @GetMapping public ApiResponse<?> list(){var now=java.time.LocalDateTime.now();return ApiResponse.ok(devices.findAll(Sort.by(Sort.Direction.DESC,"lastSeenAt")).stream().map(d->java.util.Map.of("id",d.getId(),"deviceCode",d.getDeviceCode(),"deviceName",java.util.Objects.toString(d.getDeviceName(),""),"lastIp",java.util.Objects.toString(d.getLastIp(),""),"enabled",d.isEnabled(),"online",d.getLastSeenAt()!=null&&d.getLastSeenAt().isAfter(now.minusMinutes(2)),"activity",d.getLastSeenAt()!=null&&d.getLastSeenAt().isAfter(now.minusHours(1))?"高":d.getLastSeenAt()!=null&&d.getLastSeenAt().isAfter(now.minusDays(1))?"中":"低","visitCount",d.getVisitCount(),"firstSeenAt",d.getFirstSeenAt(),"lastSeenAt",d.getLastSeenAt())).toList());}
 @PatchMapping("/{id}/enabled") public ApiResponse<Void> enabled(@PathVariable long id,@RequestParam boolean enabled){var d=devices.findById(id).orElseThrow(()->new IllegalArgumentException("设备不存在"));d.setEnabled(enabled);devices.save(d);return ApiResponse.ok(null);}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id){devices.deleteById(id);return ApiResponse.ok(null);}
}