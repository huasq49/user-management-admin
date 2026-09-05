package com.usermonitor.server.service;
import com.usermonitor.server.model.UserAccount;
import com.usermonitor.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.*;
@Service @RequiredArgsConstructor
public class AdminUserService {
 private final UserAccountRepository accounts;private final UserSessionRepository sessions;private final SharedPresetRepository presets;private final ChatMessageRepository chatMessages;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(11);private static final SecureRandom RNG=new SecureRandom();private static final char[] CHARS="ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
 @Transactional public UserAccount update(long id,Map<String,Object> r){UserAccount u=get(id);if(r.containsKey("username")){String n=Objects.toString(r.get("username"),"").trim();if(!n.matches("[\\p{L}\\p{N}_-]{2,20}"))throw new IllegalArgumentException("账号名需为2-20位文字、字母、数字、下划线或横线");accounts.findByUsernameIgnoreCase(n).filter(x->!x.getId().equals(id)).ifPresent(x->{throw new IllegalArgumentException("账号名已存在");});u.setUsername(n);}if(r.containsKey("enabled"))u.setEnabled(Boolean.parseBoolean(Objects.toString(r.get("enabled"))));sessions.deleteByUserId(id);return accounts.save(u);}
 @Transactional public Map<String,Object> resetPassword(long id){UserAccount u=get(id);String p=random(16);u.setPasswordHash(encoder.encode(p));accounts.save(u);sessions.deleteByUserId(id);return Map.of("id",u.getId(),"accountId",u.getAccountId(),"username",u.getUsername(),"password",p);}
 @Transactional public void logout(long id){get(id);sessions.deleteByUserId(id);}
 @Transactional public void delete(long id){UserAccount u=get(id);sessions.deleteByUserId(id);chatMessages.deleteByUserId(id);presets.deleteByUploaderId(id);accounts.delete(u);}
 private UserAccount get(long id){return accounts.findById(id).orElseThrow(()->new IllegalArgumentException("用户不存在"));}
 private static String random(int n){StringBuilder b=new StringBuilder();for(int i=0;i<n;i++)b.append(CHARS[RNG.nextInt(CHARS.length)]);return b.toString();}
}