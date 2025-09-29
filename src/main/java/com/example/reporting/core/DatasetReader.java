
package com.example.reporting.core;

import com.example.reporting.domain.DatasetDef;
import com.example.reporting.domain.SourceDef;
import java.util.List;
import java.util.Map;

public interface DatasetReader {
    boolean supports(SourceDef src);
    List<Map<String,Object>> read(DatasetDef def, Map<String,Object> params);
}
