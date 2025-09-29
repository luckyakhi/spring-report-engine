package com.example.reporting.domain;

import java.util.List;

public record TemplateDef(
        String path,
        List<SheetBinding> sheets
) {}
