
package com.example.reporting.core;

import com.example.reporting.domain.DatasetDef;
import com.example.reporting.domain.SourceDef;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatasetService {
    private final List<DatasetReader> readers;

    public DatasetService(List<DatasetReader> readers) {
        this.readers = readers;
    }

    public List<Map<String,Object>> read(DatasetDef def, Map<String,Object> params) {
        SourceDef src = def.source();
        return readers.stream()
                .filter(r -> r.supports(src))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No reader for source kind: " + src.kind()))
                .read(def, params);
    }
}
