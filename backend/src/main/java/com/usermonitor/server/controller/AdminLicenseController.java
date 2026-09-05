package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.dto.Requests;
import com.usermonitor.server.model.LicenseKey;
import com.usermonitor.server.repository.*;
import com.usermonitor.server.service.LicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminLicenseController {
 private final LicenseService service;private final LicenseRepository licenses;private final DeviceRepository devices;private final com.usermonitor.server.service.ActivityService activity;
 @GetMapping("/stats") public ApiResponse<?> stats(){return ApiResponse.ok(service.stats());}
 @GetMapping("/stats/history") public ApiResponse<?> history(@RequestParam(defaultValue="24") int hours){return ApiResponse.ok(activity.history(hours));}
 @PostMapping("/licenses/generate") public ApiResponse<?> generate(@Valid @RequestBody Requests.GenerateKeys r){return ApiResponse.ok(service.generate(r));}
 @GetMapping("/licenses") public ApiResponse<?> licenses(){return ApiResponse.ok(licenses.findAll(Sort.by(Sort.Direction.DESC,"id")));}
 @PatchMapping("/licenses/{id}/status") public ApiResponse<Void> status(@PathVariable long id,@RequestParam LicenseKey.Status status){service.setLicenseStatus(id,status);return ApiResponse.ok(null);}
 @PutMapping("/licenses/{id}") public ApiResponse<?> update(@PathVariable long id,@Valid @RequestBody Requests.LicenseUpdate r){return ApiResponse.ok(service.updateLicense(id,r));}
 @GetMapping("/licenses/{id}/devices") public ApiResponse<?> licenseDevices(@PathVariable long id){LicenseKey k=licenses.findById(id).orElseThrow(()->new IllegalArgumentException("卡密不存在"));return ApiResponse.ok(devices.findAll().stream().filter(d->d.getLicense().getId().equals(k.getId())).map(d->Map.of("id",d.getId(),"deviceCode",d.getDeviceCode(),"deviceName",Objects.toString(d.getDeviceName(),""),"enabled",d.isEnabled(),"boundAt",d.getBoundAt(),"lastSeenAt",Objects.toString(d.getLastSeenAt(),""),"lastIp",Objects.toString(d.getLastIp(),""))).toList());}
 @DeleteMapping("/licenses/{id}") public ApiResponse<Void> delete(@PathVariable long id){licenses.deleteById(id);return ApiResponse.ok(null);}
 @GetMapping("/devices") public ApiResponse<?> devices(){return ApiResponse.ok(devices.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(d->Map.of("id",d.getId(),"licenseId",d.getLicense().getId(),"licenseCode",d.getLicense().getCode(),"deviceCode",d.getDeviceCode(),"deviceName",Objects.toString(d.getDeviceName(),""),"enabled",d.isEnabled(),"boundAt",d.getBoundAt(),"lastSeenAt",Objects.toString(d.getLastSeenAt(),""),"lastIp",Objects.toString(d.getLastIp(),""))).toList());}
 @PatchMapping("/devices/{id}/enabled") public ApiResponse<Void> device(@PathVariable long id,@RequestParam boolean enabled){service.setDeviceEnabled(id,enabled);return ApiResponse.ok(null);}
 @PutMapping("/devices/{id}/rebind") public ApiResponse<?> rebind(@PathVariable long id,@Valid @RequestBody Requests.DeviceRebind r){return ApiResponse.ok(service.rebindDevice(id,r));}
 @DeleteMapping("/devices/{id}") public ApiResponse<Void> unbind(@PathVariable long id){devices.deleteById(id);return ApiResponse.ok(null);}
}