package com.usermonitor.server.repository;
import com.usermonitor.server.model.ClientSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
public interface ClientSessionRepository extends JpaRepository<ClientSession,Long>{ Optional<ClientSession> findByTokenAndRevokedFalse(String token); long countByRevokedFalseAndLastHeartbeatAtAfterAndExpiresAtAfter(LocalDateTime heartbeat,LocalDateTime now); long countByLoginAtAfter(LocalDateTime time); }