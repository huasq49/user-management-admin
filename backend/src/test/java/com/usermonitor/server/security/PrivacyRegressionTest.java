package com.usermonitor.server.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermonitor.server.model.UserAccount;
import com.usermonitor.server.repository.*;
import com.usermonitor.server.service.UserService;
import com.usermonitor.server.model.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PrivacyRegressionTest {
    @Test void entitySerializationNeverExposesPasswordHashOrAvatarBytes() throws Exception {
        UserAccount user = new UserAccount();
        user.setPasswordHash("sensitive-hash"); user.setAvatar(new byte[]{1,2,3});
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(user);
        assertFalse(json.contains("passwordHash"));
        assertFalse(json.contains("sensitive-hash"));
        assertFalse(new ObjectMapper().readTree(json).has("avatar"));
    }
    @Test void passwordChangeRevokesAllSessions() {
        var users = mock(UserAccountRepository.class);
        var sessions = mock(UserSessionRepository.class);
        var titles = mock(TitleApplicationRepository.class);
        var user = new UserAccount(); user.setId(7L);
        user.setPasswordHash(new BCryptPasswordEncoder(4).encode("old-test-password"));
        var session = new UserSession(); session.setUser(user);
        when(sessions.findByTokenAndExpiresAtAfter(eq("test-token"), any())).thenReturn(Optional.of(session));
        var service = new UserService(users, sessions, titles);
        service.changePassword("test-token", "old-test-password", "new-test-password", "new-test-password");
        verify(sessions).deleteByUserId(7L);
        assertTrue(new BCryptPasswordEncoder().matches("new-test-password", user.getPasswordHash()));
    }
    @Test void missingSessionIsAuthenticationFailure() {
        var sessions = mock(UserSessionRepository.class);
        when(sessions.findByTokenAndExpiresAtAfter(any(), any())).thenReturn(Optional.empty());
        var service = new UserService(mock(UserAccountRepository.class), sessions, mock(TitleApplicationRepository.class));
        assertThrows(SecurityException.class, () -> service.require("invalid"));
    }
}