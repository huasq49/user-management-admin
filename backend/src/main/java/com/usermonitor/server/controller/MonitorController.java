package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.MonitoredDevice;
import com.usermonitor.server.repository.MonitoredDeviceRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController @RequestMapping("/api/client/monitor") @RequiredArgsConstructor
public class MonitorController {
 private final MonitoredDeviceRepository devices;
 public record Ping(@NotBlank String deviceCode,String deviceName,boolean visit){}
 @PostMapping @Transactional public ApiResponse<?> ping(@RequestBody Ping r,HttpServletRequest request){
MonitoredDevice d=devices.findByDeviceCode(r.deviceCode()).orElseGet(()->{MonitoredDevice n=new MonitoredDevice();n.setDeviceCode(r.deviceCode());n.setEnabled(false);return n;});
   d.setDeviceName(r.deviceName());if(r.visit())d.setVisitCount(d.getVisitCount()+1);d.setLastSeenAt(LocalDateTime.now());String x=request.getHeader("X-Forwarded-For");d.setLastIp(x==null?request.getRemoteAddr():x.split(",")[0].trim());devices.save(d);
   return ApiResponse.ok(Map.of("accepted",d.isEnabled()));
 }
}