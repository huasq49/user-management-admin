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
public class UserService {
 private final UserAccountRepository users; private final UserSessionRepository sessions; private final TitleApplicationRepository titleApplications;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(11); private static final SecureRandom RNG=new SecureRandom();
 @Transactional public Map<String,Object> register(String username,String password,String confirm,String deviceCode,String deviceName,byte[] avatar,String avatarType,String ip){
  String name=username==null?"":username.trim(); if(!name.matches("[\\p{L}\\p{N}_-]{2,20}"))throw new IllegalArgumentException("账号名需为2-20位文字、字母、数字、下划线或横线");
  if(password==null||password.length()<6||password.length()>64)throw new IllegalArgumentException("密码需为6-64位"); if(!password.equals(confirm))throw new IllegalArgumentException("两次密码不一致");
  if(users.findByUsernameIgnoreCase(name).isPresent()||users.findByAccountId(name).isPresent())throw new IllegalArgumentException("账号名或ID已存在");
  UserAccount u=new UserAccount();u.setAccountId(uniqueId());u.setUsername(name);u.setPasswordHash(encoder.encode(password));u.setDeviceCode(null);u.setDeviceName(null);u.setLastIp(cleanIp(ip));if(avatar!=null&&avatar.length>0){if(avatar.length>200*1024)throw new IllegalArgumentException("压缩后的头像不能超过200KB");u.setAvatar(avatar);u.setAvatarContentType(AvatarImages.contentType(avatar));}users.save(u);return profile(u);
 }
 @Transactional public Map<String,Object> login(String login,String password,String deviceCode,String ip){UserAccount u=find(login);if(!u.isEnabled())throw new IllegalArgumentException("账号已禁用");if(!encoder.matches(password,u.getPasswordHash()))throw new IllegalArgumentException("账号或密码错误");sessions.deleteByUserId(u.getId());UserSession s=new UserSession();s.setUser(u);s.setToken(token());s.setExpiresAt(LocalDateTime.now().plusDays(3));sessions.save(s);u.setLastLoginAt(LocalDateTime.now());u.setLastIp(cleanIp(ip));u.setDeviceCode(null);u.setDeviceName(null);users.save(u);Map<String,Object> out=new LinkedHashMap<>(profile(u));out.put("token",s.getToken());out.put("expiresAt",s.getExpiresAt());return out;}
 public UserAccount require(String token){return sessions.findByTokenAndExpiresAtAfter(token,LocalDateTime.now()).map(UserSession::getUser).filter(UserAccount::isEnabled).orElseThrow(()->new SecurityException("登录已过期，请重新登录"));}
 public Map<String,Object> profile(UserAccount u){Map<String,Object> m=new LinkedHashMap<>();m.put("id",u.getId());m.put("accountId",u.getAccountId());m.put("username",u.getUsername());m.put("avatarUrl",u.getAvatar()==null?"":"/api/client/users/"+u.getAccountId()+"/avatar");m.put("createdAt",u.getCreatedAt());m.put("systemRole",Objects.toString(u.getSystemRole(),"USER"));m.put("customTitle",Objects.toString(u.getCustomTitle(),""));return m;}
 @Transactional public Map<String,Object> applyTitle(String token,String title,String reason){UserAccount u=require(token);String t=title==null?"":title.trim();if(!t.matches("[^\\s]{2,32}"))throw new IllegalArgumentException("自定义头衔需为2-32个非空白字符");if(t.equalsIgnoreCase("作者")||t.equalsIgnoreCase("管理员")||t.equalsIgnoreCase("AUTHOR")||t.equalsIgnoreCase("SYSTEM_ADMIN"))throw new IllegalArgumentException("不能申请作者或管理员头衔");TitleApplication a=new TitleApplication();a.setUser(u);a.setTitle(t);a.setReason(Objects.toString(reason,"").trim());return titleDto(titleApplications.save(a));}
 @Transactional(readOnly=true) public List<Map<String,Object>> titleApplications(String token){UserAccount u=require(token);return titleApplications.findByUserIdOrderByIdDesc(u.getId()).stream().map(this::titleDto).toList();}
 private Map<String,Object> titleDto(TitleApplication a){Map<String,Object> m=new LinkedHashMap<>();m.put("id",a.getId());m.put("title",a.getTitle());m.put("reason",a.getReason());m.put("status",a.getStatus().name());m.put("createdAt",a.getCreatedAt());return m;}
 @Transactional public Map<String,Object> changeUsername(String token,String username){UserAccount u=require(token);String name=username==null?"":username.trim();if(!name.matches("[\\p{L}\\p{N}_-]{2,20}"))throw new IllegalArgumentException("名字需为2-20位文字、字母、数字、下划线或横线");users.findByUsernameIgnoreCase(name).filter(x->!x.getId().equals(u.getId())).ifPresent(x->{throw new IllegalArgumentException("名字已被使用");});u.setUsername(name);return profile(users.save(u));}
 @Transactional public void changePassword(String token,String oldPassword,String newPassword,String confirm){UserAccount u=require(token);if(!encoder.matches(oldPassword==null?"":oldPassword,u.getPasswordHash()))throw new IllegalArgumentException("原密码错误");if(newPassword==null||newPassword.length()<6||newPassword.length()>64)throw new IllegalArgumentException("新密码需为6-64位");if(!newPassword.equals(confirm))throw new IllegalArgumentException("两次新密码不一致");u.setPasswordHash(encoder.encode(newPassword));users.save(u);sessions.deleteByUserId(u.getId());}
 @Transactional public Map<String,Object> changeAvatar(String token,byte[] data,String type){UserAccount u=require(token);if(data==null||data.length==0)throw new IllegalArgumentException("请选择头像");if(data.length>200*1024)throw new IllegalArgumentException("头像不能超过200KB");u.setAvatar(data);u.setAvatarContentType(AvatarImages.contentType(data));return profile(users.save(u));}
 public UserAccount find(String login){return users.findByUsernameIgnoreCase(login.trim()).or(()->users.findByAccountId(login.trim())).orElseThrow(()->new IllegalArgumentException("账号或密码错误"));}
 private static String cleanIp(String ip){if(ip==null)return "";String x=ip.split(",")[0].trim();return x.length()>64?x.substring(0,64):x;}
 private String uniqueId(){String x;do{x=String.format(Locale.ROOT,"%09d",100000000+RNG.nextInt(900000000));}while(users.findByAccountId(x).isPresent());return x;}private String token(){byte[] b=new byte[32];RNG.nextBytes(b);return HexFormat.of().formatHex(b);}
}