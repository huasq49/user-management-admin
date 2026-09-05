package com.usermonitor.server.repository;
import com.usermonitor.server.model.LicenseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
public interface LicenseRepository extends JpaRepository<LicenseKey,Long>{ Optional<LicenseKey> findByCode(String code); long countByCreatedAtAfter(LocalDateTime time); long countByStatus(LicenseKey.Status status); }