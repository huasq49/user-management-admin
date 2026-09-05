package com.usermonitor.server.repository;
import com.usermonitor.server.model.RuntimeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RuntimeConfigRepository extends JpaRepository<RuntimeConfig,Long>{}