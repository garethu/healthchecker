package com.garethyoo.healthchecker.service;

import java.net.URI;

public interface SiteChecker {
    SiteCheckResult check(String name, URI uri);
}
