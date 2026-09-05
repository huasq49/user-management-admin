package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/admin/titles") @RequiredArgsConstructor
public class AdminTitleController {
 private final TitleApplicationRepository applications; private final UserAccountRepository users;
 @GetMapping public ApiResponse<?> list(){return ApiResponse.ok(applications.findAllByOrderByIdDesc().stream().map(this::dto).toList());}
 @PutMapping("/users/{id}") @Transactional public ApiResponse<?> grant(@PathVariable long id,@RequestParam String role,@RequestParam(defaultValue="") String title){UserAccount u=users.findById(id).orElseThrow(()->new IllegalArgumentException("用户不存在"));String r=role.toUpperCase(Locale.ROOT);if(!r.equals("USER")&&!r.equals("AUTHOR")&&!r.equals("SYSTEM_ADMIN"))throw new IllegalArgumentException("头衔身份无效");u.setSystemRole(r);String t=title.trim();if(t.length()>32)throw new IllegalArgumentException("头衔不能超过32字");u.setCustomTitle(t.isEmpty()?(r.equals("AUTHOR")?"作者":r.equals("SYSTEM_ADMIN")?"管理员":null):t);users.save(u);return ApiResponse.ok(Map.of("id",u.getId(),"accountId",u.getAccountId(),"username",u.getUsername(),"systemRole",u.getSystemRole(),"customTitle",Objects.toString(u.getCustomTitle(),"")));}
 @PatchMapping("/{id}") @Transactional public ApiResponse<?> review(@PathVariable long id,@RequestParam String status){TitleApplication a=applications.findById(id).orElseThrow(()->new IllegalArgumentException("头衔申请不存在"));TitleApplication.Status s;try{s=TitleApplication.Status.valueOf(status.toUpperCase(Locale.ROOT));}catch(Exception e){throw new IllegalArgumentException("状态必须为 APPROVED 或 REJECTED");}if(s==TitleApplication.Status.PENDING)throw new IllegalArgumentException("不能将申请重新设为待审核");a.setStatus(s);a.setReviewedAt(LocalDateTime.now());if(s==TitleApplication.Status.APPROVED){a.getUser().setCustomTitle(a.getTitle());users.save(a.getUser());}return ApiResponse.ok(dto(applications.save(a)));}
 private Map<String,Object> dto(TitleApplication a){Map<String,Object>m=new LinkedHashMap<>();m.put("id",a.getId());m.put("userId",a.getUser().getId());m.put("accountId",a.getUser().getAccountId());m.put("username",a.getUser().getUsername());m.put("title",a.getTitle());m.put("reason",a.getReason());m.put("status",a.getStatus().name());m.put("createdAt",a.getCreatedAt());m.put("reviewedAt",a.getReviewedAt());return m;}
}