package com.usermonitor.server.repository;
import com.usermonitor.server.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
public interface UserSessionRepository extends JpaRepository<UserSession,Long>{ Optional<UserSession> findByTokenAndExpiresAtAfter(String token,LocalDateTime now); void deleteByUserId(Long userId); boolean existsByUserIdAndExpiresAtAfter(Long userId,LocalDateTime now); }