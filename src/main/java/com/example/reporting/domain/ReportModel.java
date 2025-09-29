
package com.example.reporting.domain;

import java.util.List;
import java.util.Map;

public record ReportDefinition(
        String reportCode,
        String version,
        TemplateDef template,
        List<DatasetDef> datasets,
        OutputDef output
) {}

public record TemplateDef(
        String path,
        List<SheetBinding> sheets
) {}

public record SheetBinding(
        String name,
        List<Binding> bindings
) {}

public record Binding(
        String type,           // singleCell | table
        String cell,           // for singleCell
        String area,           // for table anchor (top-left)
        String dataset,        // dataset name for table
        List<Column> columns,
        String expr,           // expression for singleCell
        String format,
        String style
) {}

public record Column(
        String header,
        String field,
        String format,
        String style
) {}

public record DatasetDef(
        String name,
        SourceDef source,
        List<Map<String,Object>> transforms,
        List<Map<String,Object>> validations,
        Boolean cache
) {}

public record SourceDef(
        String kind,          // jdbc|csv|rest|athena etc.
        String connection,    // bean name / JNDI / DS key
        String query,         // for jdbc/athena
        String path,          // for files
        Map<String,Object> options
) {}

public record OutputDef(
        String filenamePattern,
        String storage
) {}
