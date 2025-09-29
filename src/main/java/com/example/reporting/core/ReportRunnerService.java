
package com.example.reporting.core;

import com.example.reporting.domain.DatasetDef;
import com.example.reporting.domain.ReportDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

@Service
public class ReportRunnerService {

    private final ReportRegistry registry;
    private final DatasetService datasetService;
    private final ExcelRenderer renderer;
    private final Path outputDir;

    public ReportRunnerService(ReportRegistry registry,
                               DatasetService datasetService,
                               ExcelRenderer renderer,
                               @Value("${reporting.output-dir}") String outputDir) {
        this.registry = registry;
        this.datasetService = datasetService;
        this.renderer = renderer;
        this.outputDir = Path.of(outputDir);
    }

    public Path run(String code, String version, Map<String,Object> params) throws Exception {
        ReportRegistry.LoadedReport loaded = registry.load(code, version);
        ReportDefinition def = loaded.def();
        InputStream tpl = loaded.templateStream();

        Map<String, List<Map<String,Object>>> data = new LinkedHashMap<>();
        for (DatasetDef ds : def.datasets()) {
            List<Map<String,Object>> rows = datasetService.read(ds, params);
            data.put(ds.name(), rows);
        }
        return renderer.render(def, tpl, data, params, outputDir);
    }
}
