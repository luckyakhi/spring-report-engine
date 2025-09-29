package com.example.reporting.domain;

public record Column(
        String header,
        String field,
        String format,
        String style
) {}
