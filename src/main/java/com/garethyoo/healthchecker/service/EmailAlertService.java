package com.garethyoo.healthchecker.service;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import com.garethyoo.healthchecker.model.ServiceStatus;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertService.class);
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;
    private final MonitoringProperties properties;

    public EmailAlertService(JavaMailSender mailSender, MonitoringProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendDownAlert(ServiceStatus status) {
        send("[ALERT] " + status.name() + " is DOWN", buildBody(status));
    }

    public void sendRecoveryAlert(ServiceStatus status) {
        send("[RECOVERY] " + status.name() + " is UP", buildBody(status));
    }

    private void send(String subject, String body) {
        if (!properties.getAlerts().isEnabled() || properties.getAlerts().getRecipients().isEmpty()) {
            logger.info("Email alerts disabled or no recipients configured, skipping send");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (properties.getAlerts().getFrom() != null && !properties.getAlerts().getFrom().isBlank()) {
                message.setFrom(properties.getAlerts().getFrom());
            }
            message.setTo(properties.getAlerts().getRecipients().toArray(String[]::new));
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            logger.error("Failed to send email alert", ex);
        }
    }

    private String buildBody(ServiceStatus status) {
        return "Service: " + status.name() + System.lineSeparator()
                + "URL: " + status.url() + System.lineSeparator()
                + "State: " + status.state() + System.lineSeparator()
                + "HTTP Code: " + status.statusCode() + System.lineSeparator()
                + "Response Time: " + status.responseTimeMs() + " ms" + System.lineSeparator()
                + "Details: " + status.details() + System.lineSeparator()
                + "Checked At: " + TS_FORMAT.format(status.checkedAt());
    }
}
