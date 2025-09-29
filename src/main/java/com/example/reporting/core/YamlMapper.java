
package com.example.reporting.core;

import com.example.reporting.domain.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class YamlMapper {
    public static ReportDefinition mapToReportDefinition(Map<String,Object> m) {
        String reportCode = (String) m.get("reportCode");
        String version = String.valueOf(m.get("version"));
        Map<String,Object> tpl = (Map<String,Object>) m.get("template");
        TemplateDef template = new TemplateDef(
                (String) tpl.get("path"),
                readSheets((List<Map<String,Object>>) tpl.get("sheets"))
        );
        List<DatasetDef> datasets = new ArrayList<>();
        List<Map<String,Object>> dsList = (List<Map<String,Object>>) m.get("datasets");
        if (dsList != null) {
            for (Map<String,Object> dsm : dsList) {
                Map<String,Object> src = (Map<String,Object>) dsm.get("source");
                SourceDef source = new SourceDef(
                        (String) src.get("kind"),
                        (String) src.get("connection"),
                        (String) src.get("query"),
                        (String) src.get("path"),
                        (Map<String,Object>) src.get("options")
                );
                datasets.add(new DatasetDef(
                        (String) dsm.get("name"),
                        source,
                        (List<Map<String,Object>>) dsm.get("transforms"),
                        (List<Map<String,Object>>) dsm.get("validations"),
                        (Boolean) dsm.getOrDefault("cache", Boolean.FALSE)
                ));
            }
        }
        Map<String,Object> outm = (Map<String,Object>) m.get("output");
        OutputDef out = new OutputDef((String) outm.get("filenamePattern"), (String) outm.get("storage"));
        return new ReportDefinition(reportCode, version, template, datasets, out);
    }

    private static List<SheetBinding> readSheets(List<Map<String,Object>> lst) {
        if (lst == null) return List.of();
        List<SheetBinding> res = new ArrayList<>();
        for (Map<String,Object> sm : lst) {
            String name = (String) sm.get("name");
            List<Map<String,Object>> bms = (List<Map<String,Object>>) sm.get("bindings");
            List<Binding> bs = new ArrayList<>();
            if (bms != null) {
                for (Map<String,Object> bm : bms) {
                    List<Map<String,Object>> cols = (List<Map<String,Object>>) bm.get("columns");
                    List<Column> columns = new ArrayList<>();
                    if (cols != null) {
                        for (Map<String,Object> cm : cols) {
                            columns.add(new Column(
                                    (String) cm.get("header"),
                                    (String) cm.get("field"),
                                    (String) cm.get("format"),
                                    (String) cm.get("style")
                            ));
                        }
                    }
                    bs.add(new Binding(
                            (String) bm.get("type"),
                            (String) bm.get("cell"),
                            (String) bm.get("area"),
                            (String) bm.get("dataset"),
                            columns,
                            (String) bm.get("expr"),
                            (String) bm.get("format"),
                            (String) bm.get("style")
                    ));
                }
            }
            res.add(new SheetBinding(name, bs));
        }
        return res;
    }
}
