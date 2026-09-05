package com.usermonitor.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermonitor.server.model.SharedPreset;
import com.usermonitor.server.repository.*;
import com.usermonitor.server.service.*;
import com.usermonitor.server.security.AdminInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PresetPreviewTest {
    final ObjectMapper mapper = new ObjectMapper();
    final PresetPreviewService previews = new PresetPreviewService(mapper);
    final SharedPresetRepository presets = mock(SharedPresetRepository.class);
    final ReviewerService reviewers = mock(ReviewerService.class);
    MockMvc admin;
    MockMvc reviewer;
    byte[] file;

    // Independently derive the key using the Android PresetTransfer algorithm.
    byte[] fixture() throws Exception {
        String[] parts = {"Miao", "7e5f", "Preset", "3218", "mnd", "9"};
        for (int i=0;i<parts.length;i+=2) parts[i]=new StringBuilder(parts[i]).reverse().toString();
        byte[] key=MessageDigest.getInstance("SHA-256").digest((String.join("|",parts)+"::local-format-v1").getBytes(StandardCharsets.UTF_8));
        byte[] iv=new byte[12]; new java.security.SecureRandom().nextBytes(iv);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
        byte[] plain=mapper.writeValueAsBytes(Map.of("name","测试预设","mode","realtime","prefixEnabled",true,"prefixText","喵：","appendEnabled",true,"appendText","喵","emoticonEnabled",true,"rules","我=本喵\n你=主人","emoticons","(=^･ω･^=)"));
        byte[] encrypted=cipher.doFinal(plain);
        return ByteBuffer.allocate(16+encrypted.length).put("MND1".getBytes(StandardCharsets.US_ASCII)).put(iv).put(encrypted).array();
    }

    @BeforeEach void setup() throws Exception {
        file=fixture(); SharedPreset p=new SharedPreset(); p.setId(1L); p.setFileData(file); p.setPublished(false);
        when(presets.findById(1L)).thenReturn(Optional.of(p));
        when(presets.findWithUploaderById(1L)).thenReturn(Optional.of(p));
        doThrow(new SecurityException("审核员登录已失效")).when(reviewers).require(null);
        doThrow(new SecurityException("审核员登录已失效")).when(reviewers).require("Bearer invalid");
        AdminAuthService auth=mock(AdminAuthService.class); when(auth.valid("valid")).thenReturn(true);
        admin=MockMvcBuilders.standaloneSetup(new AdminUserPresetController(mock(UserAccountRepository.class),mock(UserSessionRepository.class),presets,mock(UserService.class),mock(AdminUserService.class),previews))
            .addInterceptors(new AdminInterceptor(auth,mapper)).setControllerAdvice(new ExceptionControllerAdvice()).build();
        reviewer=MockMvcBuilders.standaloneSetup(new ReviewerController(reviewers,presets,previews))
            .setControllerAdvice(new ExceptionControllerAdvice()).build();
    }
    @Test void androidFormatDecrypts() {
        Map<String,Object> result=previews.preview(file);
        assertEquals("测试预设",result.get("name")); assertEquals(true,result.get("prefixEnabled"));
        assertEquals("我=本喵\n你=主人",result.get("rules"));
        assertEquals("(=^･ω･^=)",result.get("emoticons"));
    }
    @Test void corruptedFilesRejected() {
        byte[] corrupt=file.clone(); corrupt[corrupt.length-1]^=1;
        assertThrows(IllegalArgumentException.class,()->previews.preview(corrupt));
        assertThrows(IllegalArgumentException.class,()->previews.preview(new byte[10]));
        assertThrows(IllegalArgumentException.class,()->previews.preview(null));
    }
    @Test void adminCanPreviewUnpublishedWithoutDownloading() throws Exception {
        admin.perform(get("/api/admin/presets/1/preview").header("Authorization","Bearer valid"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("测试预设"))
            .andExpect(jsonPath("$.data.fileData").doesNotExist());
        verify(presets,never()).save(any());
    }
    @Test void reviewerCanPreviewUnpublished() throws Exception {
        reviewer.perform(get("/api/reviewer/presets/1/preview").header("Authorization","Bearer valid"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.rules").value("我=本喵\n你=主人"));
        verify(reviewers).require("Bearer valid"); verify(presets,never()).save(any());
    }
    @Test void unauthorizedAdminCannotPreview() throws Exception {
        admin.perform(get("/api/admin/presets/1/preview")).andExpect(status().isUnauthorized());
        verifyNoInteractions(presets);
    }
    @Test void unauthorizedReviewerCannotPreview() throws Exception {
        reviewer.perform(get("/api/reviewer/presets/1/preview")).andExpect(status().isUnauthorized());
        reviewer.perform(get("/api/reviewer/presets/1/preview").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        verifyNoInteractions(presets);
    }
    @Test void missingPresetHasClearError() throws Exception {
        reviewer.perform(get("/api/reviewer/presets/999/preview").header("Authorization","Bearer valid"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("预设不存在"));
    }
}
