package com.usermonitor.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermonitor.server.ApiResponse;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/** v1 compatibility: TLS is mandatory because the request body is not signed. */
@Component
public class ClientSignatureInterceptor implements HandlerInterceptor {
    private static final long WINDOW = 300;
    private static final int MAX_NONCES = 100_000;
    private final ObjectMapper mapper;
    private final byte[] secret;
    private final Map<String, Long> nonces = new HashMap<>();
    private long nextCleanup;

    public ClientSignatureInterceptor(ObjectMapper mapper, @Value("${app.client-secret}") String secret) {
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String ts = req.getHeader("X-Timestamp"), nonce = req.getHeader("X-Nonce");
        String signature = req.getHeader("X-Signature");
        long now = Instant.now().getEpochSecond(), time;
        try { time = Long.parseLong(ts); }
        catch (NumberFormatException e) { return reject(res, 401, "缺少有效时间戳"); }
        // Subtraction/abs on an untrusted long can overflow.
        if (time < now - WINDOW || time > now + WINDOW
                || nonce == null || !nonce.matches("[A-Za-z0-9_-]{16,128}")
                || signature == null || !signature.matches("[a-fA-F0-9]{64}")) {
            return reject(res, 401, "签名参数无效");
        }
        String raw = ts + '\n' + nonce + '\n' + req.getMethod() + '\n' + req.getRequestURI();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        if (!MessageDigest.isEqual(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)),
                HexFormat.of().parseHex(signature))) return reject(res, 401, "签名错误");
        // Authenticate before insertion; reserve atomically; retain through signed expiry.
        synchronized (nonces) {
            if (now >= nextCleanup) {
                nonces.entrySet().removeIf(entry -> entry.getValue() < now);
                nextCleanup = now + 30;
            }
            if (nonces.containsKey(nonce)) return reject(res, 401, "请求已被使用");
            if (nonces.size() >= MAX_NONCES) return reject(res, 503, "签名服务繁忙，请稍后重试");
            nonces.put(nonce, time + WINDOW);
        }
        return true;
    }
    private boolean reject(HttpServletResponse res, int status, String message) throws Exception {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(mapper.writeValueAsString(ApiResponse.fail(status, message)));
        return false;
    }
}
