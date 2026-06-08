package com.garethyoo.healthchecker.service;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import com.garethyoo.healthchecker.model.ServiceState;
import com.garethyoo.healthchecker.model.ServiceStatus;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(StatusMonitorService.class);

    private final MonitoringProperties properties;
    private final SiteChecker siteChecker;
    private final EmailAlertService emailAlertService;

    private final Map<String, ServiceStatus> latestStatuses = new ConcurrentHashMap<>();
    private final Map<String, ServiceState> previousStates = new ConcurrentHashMap<>();

    public StatusMonitorService(
            MonitoringProperties properties,
            SiteChecker siteChecker,
            EmailAlertService emailAlertService
    ) {
        this.properties = properties;
        this.siteChecker = siteChecker;
        this.emailAlertService = emailAlertService;
    }

    @PostConstruct
    public void initialize() {
        for (MonitoringProperties.Target target : properties.getTargets()) {
            latestStatuses.put(target.getName(), new ServiceStatus(
                    target.getName(),
                    target.getUrl(),
                    ServiceState.CHECKING,
                    0,
                    0,
                    "Waiting for first check",
                    Instant.now()
            ));
        }
    }

    @Scheduled(fixedDelayString = "${monitor.check-interval-ms:300000}")
    public void scheduledRefresh() {
        refreshAll();
    }

    public synchronized void refreshAll() {
        for (MonitoringProperties.Target target : properties.getTargets()) {
            processTarget(target);
        }
    }

    public List<ServiceStatus> getStatuses() {
        List<ServiceStatus> statuses = new ArrayList<>();
        for (MonitoringProperties.Target target : properties.getTargets()) {
            statuses.add(latestStatuses.getOrDefault(target.getName(), new ServiceStatus(
                    target.getName(),
                    target.getUrl(),
                    ServiceState.CHECKING,
                    0,
                    0,
                    "Waiting for first check",
                    Instant.now()
            )));
        }
        return statuses;
    }

    private void processTarget(MonitoringProperties.Target target) {
        try {
            SiteCheckResult check = siteChecker.check(target.getName(), URI.create(target.getUrl()));
            ServiceState state = check.up() ? ServiceState.UP : ServiceState.DOWN;

            ServiceStatus status = new ServiceStatus(
                    target.getName(),
                    target.getUrl(),
                    state,
                    check.statusCode(),
                    check.responseTimeMs(),
                    check.details(),
                    check.checkedAt()
            );
            latestStatuses.put(target.getName(), status);

            ServiceState previous = previousStates.put(target.getName(), state);
            if (previous != null) {
                if (previous == ServiceState.UP && state == ServiceState.DOWN) {
                    emailAlertService.sendDownAlert(status);
                }
                if (previous == ServiceState.DOWN && state == ServiceState.UP && properties.getAlerts().isSendRecovery()) {
                    emailAlertService.sendRecoveryAlert(status);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed processing target {}", target.getName(), ex);
            ServiceStatus fallback = new ServiceStatus(
                    target.getName(),
                    target.getUrl(),
                    ServiceState.DOWN,
                    -1,
                    0,
                    ex.getMessage(),
                    Instant.now()
            );
            latestStatuses.put(target.getName(), fallback);
        }
    }
}
