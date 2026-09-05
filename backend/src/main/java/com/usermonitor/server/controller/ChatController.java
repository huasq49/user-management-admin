package com.usermonitor.server.controller;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.ChatMessageRepository;
import com.usermonitor.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/client/chat") @RequiredArgsConstructor
public class ChatController {
 private final ChatMessageRepository messages; private final UserService users; private final SimpMessagingTemplate realtime; private final com.usermonitor.server.service.IpRegionService regions;
 @GetMapping("/messages") @Transactional(readOnly=true) public ApiResponse<?> list(@RequestHeader("Authorization")String auth,@RequestParam(defaultValue="0")long after){users.require(UserController.bearer(auth));List<ChatMessage> list=after>0?messages.findByIdGreaterThanOrderByIdAsc(after,PageRequest.of(0,200)):messages.findAllByOrderByIdDesc(PageRequest.of(0,100));if(after<=0)Collections.reverse(list);return ApiResponse.ok(list.stream().map(this::dto).toList());}
 @PostMapping("/messages") @Transactional public ApiResponse<?> send(@RequestHeader("Authorization")String auth,@RequestBody Map<String,String> r){UserAccount u=users.require(UserController.bearer(auth));if(u.isChatBanned())throw new IllegalArgumentException("你已被禁止参与公共聊天");if(u.getChatMutedUntil()!=null&&u.getChatMutedUntil().isAfter(LocalDateTime.now()))throw new IllegalArgumentException("你已被禁言至 "+u.getChatMutedUntil());String content=Objects.toString(r.get("content"),"").trim();if(content.isBlank())throw new IllegalArgumentException("消息不能为空");if(content.length()>500)throw new IllegalArgumentException("消息不能超过500字");ChatMessage m=new ChatMessage();m.setUser(u);m.setContent(content);ChatMessage saved=messages.save(m);realtime.convertAndSend("/topic/chat",dto(saved));return ApiResponse.ok(dto(saved));}
 @PatchMapping("/messages/{id}/pin") @Transactional public ApiResponse<?> pin(@RequestHeader("Authorization")String auth,@PathVariable long id,@RequestParam boolean pinned){UserAccount u=users.require(UserController.bearer(auth));ChatMessage m=messages.findById(id).orElseThrow(()->new IllegalArgumentException("消息不存在"));boolean allowed="AUTHOR".equalsIgnoreCase(Objects.toString(u.getSystemRole(),""))||"SYSTEM_ADMIN".equalsIgnoreCase(Objects.toString(u.getSystemRole(),""));if(!allowed)throw new SecurityException("只有管理员或作者可以置顶消息");m.setPinned(pinned);ChatMessage saved=messages.save(m);realtime.convertAndSend("/topic/chat",dto(saved));return ApiResponse.ok(dto(saved));}
 private Map<String,Object> dto(ChatMessage m){UserAccount u=m.getUser();Map<String,Object>d=new LinkedHashMap<>();d.put("id",m.getId());d.put("content",m.getContent());d.put("createdAt",m.getCreatedAt());d.put("userId",u.getId());d.put("accountId",u.getAccountId());d.put("username",u.getUsername());d.put("avatarUrl",u.getAvatar()==null?"":"/api/client/users/"+u.getAccountId()+"/avatar");String region=regions.province(u.getLastIp());d.put("ipRegion",region);d.put("ipAddress",region);d.put("systemRole",Objects.toString(u.getSystemRole(),"USER"));d.put("customTitle",Objects.toString(u.getCustomTitle(),""));d.put("pinned",m.isPinned());return d;}
}