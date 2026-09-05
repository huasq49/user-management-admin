package com.usermonitor.server.repository;
import com.usermonitor.server.model.SharedPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;
public interface SharedPresetRepository extends JpaRepository<SharedPreset,Long>{
 @EntityGraph(attributePaths="uploader") List<SharedPreset> findByPublishedTrueAndPublicVisibleTrueOrderByCreatedAtDesc();
 @EntityGraph(attributePaths="uploader") List<SharedPreset> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);
 @EntityGraph(attributePaths="uploader") List<SharedPreset> findByPublishedTrueOrderByCreatedAtDesc();
 @EntityGraph(attributePaths="uploader") List<SharedPreset> findAllByOrderByIdDesc();
 @EntityGraph(attributePaths="uploader") Optional<SharedPreset> findWithUploaderById(Long id);
 void deleteByUploaderId(Long uploaderId);
}