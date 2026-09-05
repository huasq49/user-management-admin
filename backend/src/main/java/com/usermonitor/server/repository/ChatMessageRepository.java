package com.usermonitor.server.repository;

import com.usermonitor.server.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
 List<ChatMessage> findByIdGreaterThanOrderByIdAsc(Long after, Pageable pageable);
 List<ChatMessage> findAllByOrderByIdDesc(Pageable pageable);
 void deleteByUserId(Long userId);
}
