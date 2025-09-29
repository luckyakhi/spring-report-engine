package com.example.reporting.domain;

import java.util.List;
import java.util.Map;

public record DatasetDef(
        String name,
        SourceDef source,
        List<Map<String, Object>> transforms,
        List<Map<String, Object>> validations,
        Boolean cache
) {}
