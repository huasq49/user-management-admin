package com.usermonitor.server.controller;

import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/admin/chat") @RequiredArgsConstructor
public class AdminChatController {
 private final ChatMessageRepository messages;private final UserAccountRepository users;
 @GetMapping("/messages") @Transactional(readOnly=true) public ApiResponse<?> list(){return ApiResponse.ok(messages.findAllByOrderByIdDesc(PageRequest.of(0,300)).stream().map(this::dto).toList());}
 @DeleteMapping("/messages/{id}") public ApiResponse<Void> delete(@PathVariable long id){messages.deleteById(id);return ApiResponse.ok(null);}
 @DeleteMapping("/messages") public ApiResponse<Void> clear(){messages.deleteAllInBatch();return ApiResponse.ok(null);}
 @PatchMapping("/users/{id}/mute") public ApiResponse<?> mute(@PathVariable long id,@RequestParam int minutes){UserAccount u=get(id);u.setChatMutedUntil(minutes<=0?null:LocalDateTime.now().plusMinutes(Math.min(minutes,43200)));return ApiResponse.ok(user(users.save(u)));}
 @PatchMapping("/users/{id}/ban") public ApiResponse<?> ban(@PathVariable long id,@RequestParam boolean banned){UserAccount u=get(id);u.setChatBanned(banned);return ApiResponse.ok(user(users.save(u)));}
 private UserAccount get(long id){return users.findById(id).orElseThrow(()->new IllegalArgumentException("用户不存在"));}
 private Map<String,Object> dto(ChatMessage m){Map<String,Object>d=new LinkedHashMap<>();d.put("id",m.getId());d.put("content",m.getContent());d.put("createdAt",m.getCreatedAt());d.put("userId",m.getUser().getId());d.put("accountId",m.getUser().getAccountId());d.put("username",m.getUser().getUsername());d.put("chatBanned",m.getUser().isChatBanned());d.put("chatMutedUntil",m.getUser().getChatMutedUntil());return d;}
 private Map<String,Object> user(UserAccount u){Map<String,Object>d=new LinkedHashMap<>();d.put("id",u.getId());d.put("chatBanned",u.isChatBanned());d.put("chatMutedUntil",u.getChatMutedUntil());return d;}
}
