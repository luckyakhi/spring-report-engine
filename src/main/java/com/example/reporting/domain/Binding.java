package com.example.reporting.domain;

import java.util.List;

public record Binding(
        String type,
        String cell,
        String area,
        String dataset,
        List<Column> columns,
        String expr,
        String format,
        String style
) {}
