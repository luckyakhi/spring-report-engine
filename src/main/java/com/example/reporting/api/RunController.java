
package com.example.reporting.api;

import com.example.reporting.core.ReportRunnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/runs")
public class RunController {

    private final ReportRunnerService runner;

    public RunController(ReportRunnerService runner) {
        this.runner = runner;
    }

    @PostMapping
    public ResponseEntity<?> run(@RequestBody RunRequest req) throws Exception {
        Path out = runner.run(req.reportCode(), req.version(), req.params());
        return ResponseEntity.ok(Map.of("output", out.toString()));
    }

    public record RunRequest(String reportCode, String version, Map<String,Object> params) {}
}
