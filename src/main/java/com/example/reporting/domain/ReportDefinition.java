package com.example.reporting.domain;

import java.util.List;

public record ReportDefinition(
        String reportCode,
        String version,
        TemplateDef template,
        List<DatasetDef> datasets,
        OutputDef output
) {}
