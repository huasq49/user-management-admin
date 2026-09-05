package com.usermonitor.server.security;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration @RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
 private final AdminInterceptor admin; private final ClientSignatureInterceptor client;
 @Override public void addInterceptors(InterceptorRegistry r){
  r.addInterceptor(admin).addPathPatterns("/api/admin/**", "/api/overview", "/api/dashboard").excludePathPatterns("/api/admin/login");
  r.addInterceptor(client).addPathPatterns("/api/client/**").excludePathPatterns("/api/client/users/**","/api/client/presets/**","/api/client/chat/**","/api/client/announcements","/api/client/version/**");
 }
}