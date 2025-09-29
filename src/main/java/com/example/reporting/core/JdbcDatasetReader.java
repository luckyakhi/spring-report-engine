
package com.example.reporting.core;

import com.example.reporting.domain.DatasetDef;
import com.example.reporting.domain.SourceDef;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.sql.DataSource;
import java.util.*;

@Component
public class JdbcDatasetReader implements DatasetReader {

    private final Map<String, NamedParameterJdbcTemplate> templates = new HashMap<>();

    public JdbcDatasetReader(List<DataSource> dataSources) {
        // Register default DataSource under name "default"
        for (DataSource ds : dataSources) {
            templates.put("default", new NamedParameterJdbcTemplate(ds));
        }
    }

    @Override
    public boolean supports(SourceDef src) {
        return "jdbc".equalsIgnoreCase(src.kind());
    }

    @Override
    public List<Map<String, Object>> read(DatasetDef def, Map<String, Object> params) {
        SourceDef s = def.source();
        String key = Optional.ofNullable(s.connection()).orElse("default");
        NamedParameterJdbcTemplate tpl = templates.getOrDefault(key, templates.get("default"));
        return tpl.query(s.query(), params, (rs, i) -> {
            int columnCount = rs.getMetaData().getColumnCount();
            Map<String,Object> row = new LinkedCaseInsensitiveMap<>(columnCount);
            for (int k = 1; k <= columnCount; k++) {
                String name = rs.getMetaData().getColumnLabel(k);
                row.put(name, rs.getObject(k));
            }
            return row;
        });
    }
}
