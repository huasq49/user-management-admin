package com.usermonitor.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 仅在内存中解密 MND 并返回安全的文字摘要，不落盘、不向审核端发送原文件。 */
@Service
public class PresetPreviewService {
 private final ObjectMapper mapper;
 public PresetPreviewService(ObjectMapper mapper){this.mapper=mapper;}
 public Map<String,Object> preview(byte[] data){try{
  if(data==null||data.length<=32||data[0]!='M'||data[1]!='N'||data[2]!='D'||data[3]!='1')throw new IllegalArgumentException("预设文件格式无效");
  byte[] iv=Arrays.copyOfRange(data,4,16);byte[] encrypted=Arrays.copyOfRange(data,16,data.length);
  String mixed="oaiM|7e5f|teserP|3218|dnm|9::local-format-v1";
  SecretKeySpec key=new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(mixed.getBytes(StandardCharsets.UTF_8)),"AES");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,iv));
  Map<String,Object> raw=mapper.readValue(cipher.doFinal(encrypted),new TypeReference<Map<String,Object>>(){});
  Map<String,Object> out=new LinkedHashMap<>();out.put("name",Objects.toString(raw.get("name"),"未命名预设"));out.put("mode",Objects.toString(raw.get("mode"),"punctuation"));out.put("prefixEnabled",Boolean.parseBoolean(Objects.toString(raw.get("prefixEnabled"),"false")));out.put("prefixText",Objects.toString(raw.get("prefixText"),""));out.put("appendEnabled",Boolean.parseBoolean(Objects.toString(raw.get("appendEnabled"),"false")));out.put("appendText",Objects.toString(raw.get("appendText"),""));out.put("emoticonEnabled",Boolean.parseBoolean(Objects.toString(raw.get("emoticonEnabled"),"false")));out.put("rules",Objects.toString(raw.get("rules"),""));out.put("emoticons",Objects.toString(raw.get("emoticons"),""));return out;
 }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalArgumentException("无法在线预览：预设文件损坏或版本不兼容");}}
}