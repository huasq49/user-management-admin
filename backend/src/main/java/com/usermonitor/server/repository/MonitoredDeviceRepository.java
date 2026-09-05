package com.usermonitor.server.repository;
import com.usermonitor.server.model.MonitoredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
public interface MonitoredDeviceRepository extends JpaRepository<MonitoredDevice,Long>{
 Optional<MonitoredDevice> findByDeviceCode(String code);
 long countByLastSeenAtAfterAndEnabledTrue(LocalDateTime time);
 long countByFirstSeenAtAfter(LocalDateTime time);
 long countByLastSeenAtAfter(LocalDateTime time);
}