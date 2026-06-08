package com.garethyoo.healthchecker.service;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpSiteChecker implements SiteChecker {

    private static final Logger logger = LoggerFactory.getLogger(HttpSiteChecker.class);

    private final HttpClient httpClient;
    private final MonitoringProperties properties;

    public HttpSiteChecker(MonitoringProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public SiteCheckResult check(String name, URI uri) {
        Instant checkedAt = Instant.now();
        long start = System.currentTimeMillis();

        try {
            HttpResponse<Void> response = send("HEAD", uri);
            if (response.statusCode() == 405) {
                response = send("GET", uri);
            }
            long elapsed = System.currentTimeMillis() - start;
            int code = response.statusCode();
            boolean up = code >= 200 && code < 400;
            return new SiteCheckResult(up, code, elapsed, "HTTP " + code, checkedAt);
        } catch (Exception ex) {
            logger.warn("HEAD check failed for {}, trying GET fallback", name, ex);
            try {
                HttpResponse<Void> response = send("GET", uri);
                long elapsed = System.currentTimeMillis() - start;
                int code = response.statusCode();
                boolean up = code >= 200 && code < 400;
                return new SiteCheckResult(up, code, elapsed, "HTTP " + code + " (GET fallback)", checkedAt);
            } catch (Exception fallbackEx) {
                long elapsed = System.currentTimeMillis() - start;
                return new SiteCheckResult(false, -1, elapsed, fallbackEx.getMessage(), checkedAt);
            }
        }
    }

    private HttpResponse<Void> send(String method, URI uri) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(properties.getReadTimeout())
                .header("User-Agent", "UWW-HealthChecker/1.0");

        if ("HEAD".equals(method)) {
            builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());
    }
}
