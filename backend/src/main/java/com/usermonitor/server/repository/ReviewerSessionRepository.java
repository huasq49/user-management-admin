package com.usermonitor.server.repository;
import com.usermonitor.server.model.ReviewerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.LocalDateTime;
import java.util.Optional;
public interface ReviewerSessionRepository extends JpaRepository<ReviewerSession,Long>{@EntityGraph(attributePaths="reviewer") Optional<ReviewerSession> findByTokenAndExpiresAtAfter(String token,LocalDateTime now);void deleteByReviewerId(Long reviewerId);}