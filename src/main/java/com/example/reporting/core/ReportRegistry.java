
package com.example.reporting.core;

import com.example.reporting.domain.ReportDefinition;
import com.example.reporting.store.ReportConfigEntity;
import com.example.reporting.store.ReportConfigRepository;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ReportRegistry {

    private final ReportConfigRepository repo;
    private final Yaml yaml = new Yaml();

    public ReportRegistry(ReportConfigRepository repo) {
        this.repo = repo;
    }

    public LoadedReport load(String code, String version) {
        ReportConfigEntity e = repo.findFirstByReportCodeAndVersionOrderByIdDesc(code, version)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + code + ":" + version));
        Map<String,Object> map = yaml.load(e.getYamlConfig());
        ReportDefinition def = YamlMapper.mapToReportDefinition(map);
        return new LoadedReport(def, e.getTemplateBytes() != null ? new ByteArrayInputStream(e.getTemplateBytes()) : null);
    }

    public record LoadedReport(ReportDefinition def, java.io.InputStream templateStream) {}
}
