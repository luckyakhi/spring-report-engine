package com.example.reporting.domain;

import java.util.Map;

public record SourceDef(
        String kind,
        String connection,
        String query,
        String path,
        Map<String, Object> options
) {}
