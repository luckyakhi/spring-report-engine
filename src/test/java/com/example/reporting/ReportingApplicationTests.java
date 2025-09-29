package com.example.reporting;

import com.example.reporting.core.ReportRunnerService;
import com.example.reporting.store.ReportConfigEntity;
import com.example.reporting.store.ReportConfigRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

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
  sheets:
    - name: "Details"
      bindings:
        - type: table
          area: "A2"
          dataset: "details"
          columns:
            - header: "ID"
              field: "id"
            - header: "NAME"
              field: "name"
            - header: "BAL"
              field: "balance"
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

        // Create a JXLS template workbook programmatically:
        byte[] template = buildTemplate();

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

    private byte[] buildTemplate() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Details");
            // Header row
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("ID");
            h.createCell(1).setCellValue("NAME");
            h.createCell(2).setCellValue("BAL");

            // Row with JXLS expressions
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("${row.id}");
            r.createCell(1).setCellValue("${row.name}");
            r.createCell(2).setCellValue("${row.balance}");

            // Put jx:each directive in a cell comment anchored to A2
            CreationHelper factory = wb.getCreationHelper();
            Drawing<?> drawing = sh.createDrawingPatriarch();
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(3);
            anchor.setRow1(1);
            anchor.setRow2(3);
            Comment comment = drawing.createCellComment(anchor);
            comment.setString(factory.createRichTextString("jx:area(lastCell=\"C2\")\njx:each(items=\"details\" var=\"row\")"));
            sh.getRow(1).getCell(0).setCellComment(comment);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }
}
