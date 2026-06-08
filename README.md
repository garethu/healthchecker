# UWW Health Checker

Simple Spring Boot dashboard that monitors website availability and sends email alerts on status transitions.

## What It Does

- Checks configured websites on a schedule
- Shows current state on a web page with a red or green indicator
- Refreshes dashboard content dynamically using HTMX
- Sends email alerts when a site goes from UP to DOWN
- Optionally sends recovery email when a site goes from DOWN to UP

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Thymeleaf + HTMX
- Maven

## Endpoints

- Dashboard: /
- HTMX fragment: /fragments/status-cards
- Actuator health: /actuator/health

## Current Defaults

These are currently configured in src/main/resources/application.yml:

- Server port: 8080
- Check interval: 300000 ms (5 minutes)
- Targets:
  - abc -> https://www.google.com
  - zyx -> https://www.bbc.com
- Mail relay auth: disabled
- Mail relay TLS: disabled

## Prerequisites

- JDK 21 installed and on PATH
- Maven 3.9+ installed and on PATH
- Access to an SMTP relay (if email alerts are enabled)

## Run Locally

1. Open a terminal in the project root.
2. Build and test:
   mvn clean install
3. Start the app:
   mvn spring-boot:run
4. Open browser:
   http://localhost:8080

## Configuration

Edit src/main/resources/application.yml.

### Monitor Settings

- monitor.check-interval-ms: how often checks run
- monitor.connect-timeout: connection timeout
- monitor.read-timeout: read timeout
- monitor.targets: list of websites to monitor

### Alert Settings

- monitor.alerts.enabled: turn email alerts on or off
- monitor.alerts.send-recovery: send recovery messages
- monitor.alerts.from: sender email address
- monitor.alerts.recipients: list of recipient addresses

### Mail Relay (No Username/Password)

Use your company SMTP relay values:

- spring.mail.host
- spring.mail.port
- spring.mail.properties.mail.smtp.auth=false
- spring.mail.properties.mail.smtp.starttls.enable=true or false based on company policy

## Find Company SMTP Relay

A helper script is included:

- find-relay.bat

What it does:

- checks SMTP related environment variables
- detects domain from current user UPN
- queries DNS MX and SMTP related SRV records
- probes common relay hostnames on ports 25 and 587
- writes a report to smtp_relay_discovery_report.txt

How to run:

1. Open Command Prompt in project root.
2. Run:
   find-relay.bat
3. Review generated report file.

Important:

MX records usually show inbound mail gateways, not always the outbound relay for applications. Confirm final relay details with your company mail team.

## Typical IT Questions to Ask

- Approved relay hostname
- Port (25, 587, 465)
- TLS mode (none, STARTTLS, SSL)
- Whether authentication is required
- Whether source IP allowlist is required
- Approved sender domain/address

## Testing

Run:

mvn test

## Project Structure

- src/main/java/com/uww/healthchecker: application code
- src/main/resources/templates: Thymeleaf templates
- src/main/resources/static/css: styling
- src/main/resources/application.yml: runtime config
- src/test/java/com/uww/healthchecker: tests
- find-relay.bat: relay discovery helper script
