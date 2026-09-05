package com.usermonitor.server.repository;
import com.usermonitor.server.model.ActivitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface ActivitySnapshotRepository extends JpaRepository<ActivitySnapshot,Long>{List<ActivitySnapshot> findByCapturedAtAfterOrderByCapturedAtAsc(LocalDateTime time);void deleteByCapturedAtBefore(LocalDateTime time);}