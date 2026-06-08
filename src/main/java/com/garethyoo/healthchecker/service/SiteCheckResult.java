package com.garethyoo.healthchecker.service;

import java.time.Instant;

public record SiteCheckResult(
        boolean up,
        int statusCode,
        long responseTimeMs,
        String details,
        Instant checkedAt
) {
}
