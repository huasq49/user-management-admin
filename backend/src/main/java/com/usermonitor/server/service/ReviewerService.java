package com.usermonitor.server.service;
import com.usermonitor.server.model.*;
import com.usermonitor.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor
public class ReviewerService {
 private final ReviewerAccountRepository accounts; private final ReviewerSessionRepository sessions;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(11);private static final SecureRandom RNG=new SecureRandom();
 private static final char[] CHARS="ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
 @Transactional public List<Map<String,Object>> generate(int count){if(count<1||count>100)throw new IllegalArgumentException("数量须为1-100");List<Map<String,Object>> out=new ArrayList<>();for(int i=0;i<count;i++){String u;do{u="rv_"+random(10).toLowerCase(Locale.ROOT);}while(accounts.findByUsername(u).isPresent());String p=random(16);ReviewerAccount a=new ReviewerAccount();a.setUsername(u);a.setPasswordHash(encoder.encode(p));a=accounts.save(a);out.add(generated(a,p));}return out;}
 public List<Map<String,Object>> list(){return accounts.findAll().stream().map(a->{Map<String,Object>m=new LinkedHashMap<>();m.put("id",a.getId());m.put("username",a.getUsername());m.put("enabled",a.isEnabled());m.put("createdAt",a.getCreatedAt());m.put("lastLoginAt",a.getLastLoginAt());return m;}).toList();}
 @Transactional public void enabled(long id,boolean value){ReviewerAccount a=get(id);a.setEnabled(value);accounts.save(a);if(!value)sessions.deleteByReviewerId(id);}
 @Transactional public Map<String,Object> resetPassword(long id){ReviewerAccount a=get(id);String password=random(16);a.setPasswordHash(encoder.encode(password));accounts.save(a);sessions.deleteByReviewerId(id);return generated(a,password);}
 @Transactional public void delete(long id){ReviewerAccount a=get(id);sessions.deleteByReviewerId(id);accounts.delete(a);}
 @Transactional public String login(String username,String password){ReviewerAccount a=accounts.findByUsername(Objects.toString(username,"").trim()).filter(ReviewerAccount::isEnabled).orElseThrow(()->new IllegalArgumentException("账号或密码错误"));if(!encoder.matches(Objects.toString(password,""),a.getPasswordHash()))throw new IllegalArgumentException("账号或密码错误");a.setLastLoginAt(LocalDateTime.now());accounts.save(a);sessions.deleteByReviewerId(a.getId());ReviewerSession s=new ReviewerSession();s.setReviewer(a);s.setToken(randomHex(32));s.setExpiresAt(LocalDateTime.now().plusHours(12));sessions.save(s);return s.getToken();}
 @Transactional(readOnly=true) public ReviewerAccount require(String authorization){String token=authorization!=null&&authorization.startsWith("Bearer ")?authorization.substring(7):"";return sessions.findByTokenAndExpiresAtAfter(token,LocalDateTime.now()).map(ReviewerSession::getReviewer).filter(ReviewerAccount::isEnabled).orElseThrow(()->new SecurityException("审核员登录已失效"));}
 private ReviewerAccount get(long id){return accounts.findById(id).orElseThrow(()->new IllegalArgumentException("审核员不存在"));}
 private Map<String,Object> generated(ReviewerAccount a,String p){return Map.of("id",a.getId(),"username",a.getUsername(),"password",p,"enabled",a.isEnabled());}
 private static String random(int n){StringBuilder b=new StringBuilder(n);for(int i=0;i<n;i++)b.append(CHARS[RNG.nextInt(CHARS.length)]);return b.toString();}
 private static String randomHex(int n){byte[] b=new byte[n];RNG.nextBytes(b);return HexFormat.of().formatHex(b);}
}