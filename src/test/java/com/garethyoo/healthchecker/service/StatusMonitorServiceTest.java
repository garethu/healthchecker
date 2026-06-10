package com.garethyoo.healthchecker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import com.garethyoo.healthchecker.model.ServiceState;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StatusMonitorServiceTest {

    private SiteChecker siteChecker;
    private EmailAlertService emailAlertService;
    private StatusMonitorService statusMonitorService;

    @BeforeEach
    void setUp() {
        siteChecker = Mockito.mock(SiteChecker.class);
        emailAlertService = Mockito.mock(EmailAlertService.class);

        MonitoringProperties properties = new MonitoringProperties();
        MonitoringProperties.Target target = new MonitoringProperties.Target();
        target.setName("abc");
        target.setUrl("https://www.abc.com");
        properties.setTargets(List.of(target));

        statusMonitorService = new StatusMonitorService(properties, siteChecker, emailAlertService);
        statusMonitorService.initialize();
    }

    @Test
    void sendsDownAlertOnTransitionFromUpToDown() {
        when(siteChecker.check(any(), any(URI.class)))
                .thenReturn(new SiteCheckResult(true, 200, 40, "HTTP 200", Instant.now()))
                .thenReturn(new SiteCheckResult(false, 500, 100, "HTTP 500", Instant.now()));

        statusMonitorService.refreshAll();
        statusMonitorService.refreshAll();

        verify(emailAlertService, times(1)).sendDownAlert(any());
    }

    @Test
    void sendsDownAlertOnFirstDownCheck() {
        when(siteChecker.check(any(), any(URI.class)))
                .thenReturn(new SiteCheckResult(false, 503, 80, "HTTP 503", Instant.now()));

        statusMonitorService.refreshAll();

        verify(emailAlertService, times(1)).sendDownAlert(any());
        assertThat(statusMonitorService.getStatuses().getFirst().state()).isEqualTo(ServiceState.DOWN);
    }
}
