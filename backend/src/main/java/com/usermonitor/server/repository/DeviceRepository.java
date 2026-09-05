package com.usermonitor.server.repository;
import com.usermonitor.server.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.*;
public interface DeviceRepository extends JpaRepository<Device,Long>{ Optional<Device> findByLicenseAndDeviceCode(LicenseKey license,String deviceCode); long countByLicenseAndEnabledTrue(LicenseKey license); long countByBoundAtAfter(LocalDateTime time); }