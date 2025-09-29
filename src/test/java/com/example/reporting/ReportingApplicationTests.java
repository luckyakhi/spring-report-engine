package com.example.reporting;

import com.example.reporting.core.ReportRunnerService;
import com.example.reporting.store.ReportConfigEntity;
import com.example.reporting.store.ReportConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReportingApplicationTests {

    @Autowired ReportRunnerService runner;
    @Autowired ReportConfigRepository repo;

    @Test
    void runReport_withH2Config_andJxlsTemplate_rendersXlsx() throws Exception {
        // 1) Insert sample YAML config and Excel template into H2-backed JPA
        String yaml = """
reportCode: DEMO-ACCOUNTS
version: 1.0
template:
  path: inline
  sheets: []
datasets:
  - name: details
    source:
      kind: jdbc
      query: |
        SELECT id, name, balance FROM accounts ORDER BY id
output:
  filenamePattern: "DEMO_${asOfDate}_${runId}.xlsx"
  storage: "."
""";

        // Load the JXLS template workbook from the classpath:
        byte[] template = loadZippedTemplateFromDirectory("/templates/demo_accounts_template/");

        ReportConfigEntity e = new ReportConfigEntity();
        e.setReportCode("DEMO-ACCOUNTS");
        e.setVersion("1.0");
        e.setYamlConfig(yaml);
        e.setTemplateBytes(template);
        repo.save(e);

        // 2) Create sample data table in default H2 and populate rows
        try (var conn = java.sql.DriverManager.getConnection("jdbc:h2:mem:reportdb", "sa", "")) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS accounts(id INT PRIMARY KEY, name VARCHAR(64), balance DECIMAL(18,2));");
            conn.createStatement().execute("MERGE INTO accounts KEY(id) VALUES (1,'Alice',1200.50),(2,'Bob', 850.00),(3,'Cara', 0.00);");
        }

        // 3) Run
        Path output = runner.run("DEMO-ACCOUNTS", "1.0", Map.of("asOfDate", LocalDate.now().toString()));
        assertThat(Files.exists(output)).isTrue();

        // Basic sanity: file size > 0
        assertThat(Files.size(output)).isGreaterThan(1000);
}

    private byte[] loadZippedTemplateFromDirectory(String resourceDir) throws Exception {
        URL directoryUrl = getClass().getResource(resourceDir);
        assertThat(directoryUrl).as("template directory resource").isNotNull();

        Path directory = Path.of(directoryUrl.toURI());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            List<Path> files;
            try (var paths = Files.walk(directory)) {
                files = paths.filter(Files::isRegularFile)
                        .sorted()
                        .toList();
            }
            for (Path path : files) {
                String entryName = directory.relativize(path).toString().replace('\\', '/');
                ZipEntry entry = new ZipEntry(entryName);
                zipOut.putNextEntry(entry);
                Files.copy(path, zipOut);
                zipOut.closeEntry();
            }
            zipOut.finish();
            return baos.toByteArray();
        }
    }
}
