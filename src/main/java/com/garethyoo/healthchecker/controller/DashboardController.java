package com.garethyoo.healthchecker.controller;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import com.garethyoo.healthchecker.service.StatusMonitorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StatusMonitorService statusMonitorService;
    private final MonitoringProperties properties;

    public DashboardController(StatusMonitorService statusMonitorService, MonitoringProperties properties) {
        this.statusMonitorService = statusMonitorService;
        this.properties = properties;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("statuses", statusMonitorService.getStatuses());
        model.addAttribute("pollIntervalSeconds", Math.max(1, properties.getCheckIntervalMs() / 1000));
        return "dashboard";
    }

    @GetMapping("/fragments/status-cards")
    public String statusCards(Model model) {
        model.addAttribute("statuses", statusMonitorService.getStatuses());
        return "fragments/status-cards :: statusCards";
    }
}
