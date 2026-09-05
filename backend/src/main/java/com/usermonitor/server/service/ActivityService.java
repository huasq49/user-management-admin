package com.usermonitor.server.service;
import com.usermonitor.server.model.ActivitySnapshot;
import com.usermonitor.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
@Service @RequiredArgsConstructor
public class ActivityService {
 private final ActivitySnapshotRepository snapshots;private final MonitoredDeviceRepository devices;
 @Scheduled(fixedDelay=300000,initialDelay=15000) @Transactional public void sample(){LocalDateTime now=LocalDateTime.now();ActivitySnapshot s=new ActivitySnapshot();s.setCapturedAt(now);s.setOnline(devices.countByLastSeenAtAfterAndEnabledTrue(now.minusMinutes(2)));s.setActiveUsers(devices.countByLastSeenAtAfter(now.minusHours(1)));s.setVisits(devices.findAll().stream().mapToLong(x->x.getVisitCount()).sum());snapshots.save(s);snapshots.deleteByCapturedAtBefore(now.minusDays(30));}
 public Object history(int hours){int safe=Math.max(1,Math.min(hours,720));return snapshots.findByCapturedAtAfterOrderByCapturedAtAsc(LocalDateTime.now().minusHours(safe));}
}