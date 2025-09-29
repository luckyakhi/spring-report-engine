
package com.example.reporting.core;

import com.example.reporting.domain.DatasetDef;
import com.example.reporting.domain.SourceDef;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Stream;

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
            int c = rs.getMetaData().getColumnCount();
            Map<String,Object> map = new LinkedHashMap<>();
            for (int k=1; k<=c; k++) {
                String name = rs.getMetaData().getColumnLabel(k);
                map.put(name, rs.getObject(k));
            }
            return map;
        });
    }
}
