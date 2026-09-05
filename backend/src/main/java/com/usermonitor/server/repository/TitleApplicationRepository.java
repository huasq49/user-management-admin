package com.usermonitor.server.repository;
import com.usermonitor.server.model.TitleApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TitleApplicationRepository extends JpaRepository<TitleApplication,Long> {
 List<TitleApplication> findByUserIdOrderByIdDesc(Long userId);
 List<TitleApplication> findAllByOrderByIdDesc();
}