
package com.example.reporting.core;

import com.example.reporting.domain.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class ExcelRenderer {

    public Path render(ReportDefinition def,
                       InputStream templateStream,
                       Map<String, List<Map<String,Object>>> data,
                       Map<String,Object> meta,
                       Path outputDir) throws IOException {

        Files.createDirectories(outputDir);
        String fileName = def.output().filenamePattern()
                .replace("${asOfDate}", Objects.toString(meta.getOrDefault("asOfDate","")))
                .replace("${runId}", UUID.randomUUID().toString().substring(0,8));
        Path out = outputDir.resolve(fileName);

        if (templateStream == null) {
            // fallback: create a simple workbook if no template provided
            try (SXSSFWorkbook wb = new SXSSFWorkbook(100);
                 OutputStream os = Files.newOutputStream(out)) {
                for (Map.Entry<String, List<Map<String, Object>>> e : data.entrySet()) {
                    Sheet sh = wb.createSheet(e.getKey());
                    writeTable(sh, e.getValue());
                }
                wb.write(os);
            }
            return out;
        }

        Context ctx = new Context();
        for (String k : data.keySet()) ctx.putVar(k, data.get(k));
        ctx.putVar("meta", meta);

        try (Workbook templateWorkbook = WorkbookFactory.create(templateStream)) {
            PoiTransformer transformer = PoiTransformer.createSxssfTransformer(templateWorkbook, 1000, true);
            JxlsHelper.getInstance().setUseFastFormulaProcessor(true).processTemplate(ctx, transformer);
            try (OutputStream os = Files.newOutputStream(out)) {
                transformer.write(os);
            }
            return out;
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            throw new IOException("Invalid Excel template format", e);
        }
    }

    private void writeTable(Sheet sh, List<Map<String,Object>> rows) {
        if (rows == null || rows.isEmpty()) return;
        Row h = sh.createRow(0);
        int ci=0;
        for (String k : rows.get(0).keySet()) {
            h.createCell(ci++).setCellValue(k);
        }
        int r=1;
        for (Map<String,Object> row : rows) {
            Row rr = sh.createRow(r++);
            int cc=0;
            for (Object v : row.values()) {
                Cell c = rr.createCell(cc++);
                if (v instanceof Number num) c.setCellValue(num.doubleValue());
                else if (v instanceof java.util.Date dt) c.setCellValue(dt);
                else c.setCellValue(v==null?"":String.valueOf(v));
            }
        }
    }
}
