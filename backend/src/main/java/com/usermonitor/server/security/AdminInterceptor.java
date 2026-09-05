package com.usermonitor.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermonitor.server.ApiResponse;
import com.usermonitor.server.service.AdminAuthService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component @RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {
 private final AdminAuthService auth; private final ObjectMapper mapper;
 @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler)throws Exception{
  String h=req.getHeader("Authorization"); String token=h!=null&&h.startsWith("Bearer ")?h.substring(7):null;
  if(auth.valid(token)) return true;
  res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write(mapper.writeValueAsString(ApiResponse.fail(401,"管理员登录已失效")));return false;
 }
}