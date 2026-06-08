package com.garethyoo.healthchecker.model;

import java.time.Instant;

public record ServiceStatus(
        String name,
        String url,
        ServiceState state,
        int statusCode,
        long responseTimeMs,
        String details,
        Instant checkedAt
) {
}
