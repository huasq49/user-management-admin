package com.usermonitor.server.repository;
import com.usermonitor.server.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AnnouncementRepository extends JpaRepository<Announcement,Long>{ List<Announcement> findByPublishedTrueOrderByPinnedDescCreatedAtDesc(); }