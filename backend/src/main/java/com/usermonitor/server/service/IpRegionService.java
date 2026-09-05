package com.usermonitor.server.service;
import org.springframework.stereotype.Service;
/** No unverified datasets are bundled; no IPs are sent to external services. */
@Service
public class IpRegionService {
    public String province(String ip) { return "未知"; }
}
