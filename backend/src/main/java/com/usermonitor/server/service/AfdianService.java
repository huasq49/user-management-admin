package com.usermonitor.server.service;

import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.usermonitor.server.repository.RuntimeConfigRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service @RequiredArgsConstructor
public class AfdianService {
 @Value("${app.afdian.user-id:}") private String userId;
 @Value("${app.afdian.token:}") private String token;
 private final ObjectMapper mapper;
 private final RuntimeConfigRepository runtimeConfigs;
 private final RestClient client=RestClient.builder().baseUrl("https://afdian.com").build();
 public Map<String,Object> sponsors() throws Exception {
  var config=runtimeConfigs.findById(1L).orElse(null);
  String activeUserId=config!=null&&config.getAfdianUserId()!=null&&!config.getAfdianUserId().isBlank()?config.getAfdianUserId().trim():userId.trim();
  String activeToken=config!=null&&config.getAfdianToken()!=null&&!config.getAfdianToken().isBlank()?config.getAfdianToken().trim():token.trim();
  if(activeUserId.isBlank()||activeToken.isBlank()) throw new IllegalArgumentException("赞助服务尚未配置");
  List<Map<String,String>> rows=new ArrayList<>();
  for(int page=1;page<=20;page++){
   String params=mapper.writeValueAsString(Map.of("page",page));long ts=System.currentTimeMillis()/1000L;
   String sign=md5(activeToken+"params"+params+"ts"+ts+"user_id"+activeUserId);
   String body=mapper.writeValueAsString(Map.of("user_id",activeUserId,"params",params,"ts",ts,"sign",sign));
   JsonNode root=client.post().uri("/api/open/query-sponsor").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
   if(root==null||root.path("ec").asInt()!=200)throw new IllegalArgumentException("爱发电接口暂不可用");
   JsonNode list=root.path("data").path("list");if(!list.isArray()||list.isEmpty())break;
   for(JsonNode item:list){String name=item.path("user").path("name").asText("爱发电用户");String amount=item.path("all_sum_amount").asText("");String plan=item.path("sponsor_plan").path("name").asText("");rows.add(Map.of("name",name,"amount",amount,"plan",plan));}
   if(list.size()<20)break;
  }
  return Map.of("items",rows,"count",rows.size());
 }
 private static String md5(String value)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8)));}
}