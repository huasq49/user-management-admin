package com.usermonitor.server.service;

import com.usermonitor.server.model.AdminSession;
import com.usermonitor.server.repository.AdminSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.security.SecureRandom;

@Service @RequiredArgsConstructor
public class AdminAuthService {
 private final AdminSessionRepository sessions;
 @Value("${app.admin.username}") private String username;
 @Value("${app.admin.password}") private String password;
 public String login(String user,String pass){
  if(!constantEquals(username,user)||!constantEquals(password,pass)) throw new IllegalArgumentException("用户名或密码错误");
  AdminSession s=new AdminSession(); s.setToken(randomToken()); s.setExpiresAt(LocalDateTime.now().plusHours(24)); sessions.save(s); return s.getToken();
 }
 public boolean valid(String token){ return token!=null&&sessions.findByTokenAndExpiresAtAfter(token,LocalDateTime.now()).isPresent(); }
 @Transactional public void logout(String token){ sessions.findByTokenAndExpiresAtAfter(token,LocalDateTime.now()).ifPresent(sessions::delete); }
 private static boolean constantEquals(String a,String b){return b!=null&&MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));}
 private static String randomToken(){byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return HexFormat.of().formatHex(bytes);}
}