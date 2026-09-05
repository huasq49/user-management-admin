package com.usermonitor.server.repository;
import com.usermonitor.server.model.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
public interface AdminSessionRepository extends JpaRepository<AdminSession,Long>{ Optional<AdminSession> findByTokenAndExpiresAtAfter(String token,LocalDateTime now); void deleteByExpiresAtBefore(LocalDateTime now); }