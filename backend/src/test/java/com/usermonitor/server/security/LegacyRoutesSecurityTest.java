package com.usermonitor.server.security;
import com.usermonitor.server.controller.LegacyDashboardController;
import com.usermonitor.server.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class LegacyRoutesSecurityTest {
    @Test void legacyDashboardRequiresAdminToken() throws Exception {
        var auth = mock(AdminAuthService.class);
        var licenses = mock(LicenseService.class);
        var activity = mock(ActivityService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new LegacyDashboardController(licenses, activity))
            .addMappedInterceptors(new String[]{"/api/overview", "/api/dashboard"}, new AdminInterceptor(auth, new ObjectMapper()))
            .build();
        mvc.perform(get("/api/overview")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        verifyNoInteractions(licenses, activity);
    }
}