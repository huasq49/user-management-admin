package com.usermonitor.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class ClientSignatureInterceptorTest {
    private static final String SECRET = "test-only-signature-material-32-characters";
    private final ClientSignatureInterceptor interceptor = new ClientSignatureInterceptor(new ObjectMapper(), SECRET);

    private MockHttpServletRequest request(long ts, String nonce) throws Exception {
        var req = new MockHttpServletRequest("POST", "/api/client/login");
        req.addHeader("X-Timestamp", Long.toString(ts));
        req.addHeader("X-Nonce", nonce);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String raw = ts + "\n" + nonce + "\nPOST\n/api/client/login";
        req.addHeader("X-Signature", HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8))));
        return req;
    }

    @Test void acceptsValidRequestAndRejectsReplay() throws Exception {
        var req = request(Instant.now().getEpochSecond(), "unique-test-nonce-0001");
        assertTrue(interceptor.preHandle(req, new MockHttpServletResponse(), null));
        var res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(req, res, null));
        assertEquals(401, res.getStatus());
    }

    @Test void invalidSignatureDoesNotConsumeNonce() throws Exception {
        var valid = request(Instant.now().getEpochSecond(), "unique-test-nonce-0002");
        var invalid = request(Instant.now().getEpochSecond(), "unique-test-nonce-0002");
        invalid.removeHeader("X-Signature");
        invalid.addHeader("X-Signature", "0".repeat(64));
        assertFalse(interceptor.preHandle(invalid, new MockHttpServletResponse(), null));
        assertTrue(interceptor.preHandle(valid, new MockHttpServletResponse(), null));
    }

    @Test void rejectsOverflowExpiredFutureAndMalformedHeaders() throws Exception {
        long now = Instant.now().getEpochSecond();
        for (long ts : new long[]{Long.MIN_VALUE, Long.MAX_VALUE, now - 301, now + 302}) {
            assertFalse(interceptor.preHandle(request(ts, "unique-test-nonce-0003"), new MockHttpServletResponse(), null));
        }
        assertFalse(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null));
        assertFalse(interceptor.preHandle(request(now, "x".repeat(129)), new MockHttpServletResponse(), null));
    }

    @Test void acceptsUppercaseHex() throws Exception {
        var req = request(Instant.now().getEpochSecond(), "unique-test-nonce-0004");
        String signature = req.getHeader("X-Signature");
        req.removeHeader("X-Signature");
        req.addHeader("X-Signature", signature.toUpperCase(java.util.Locale.ROOT));
        assertTrue(interceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    @Test void concurrentReplayHasExactlyOneWinner() throws Exception {
        var pool = Executors.newFixedThreadPool(8);
        long now = Instant.now().getEpochSecond();
        try {
            var jobs = IntStream.range(0, 32).<Callable<Boolean>>mapToObj(i -> () ->
                interceptor.preHandle(request(now, "concurrent-nonce-0001"), new MockHttpServletResponse(), null)).toList();
            int accepted = 0;
            for (var result : pool.invokeAll(jobs)) if (result.get()) accepted++;
            assertEquals(1, accepted);
        } finally { pool.shutdownNow(); }
    }
}
