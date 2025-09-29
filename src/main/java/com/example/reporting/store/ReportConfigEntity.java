
package com.example.reporting.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_config")
@Getter @Setter @NoArgsConstructor
public class ReportConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportCode;

    @Column(nullable = false)
    private String version;

    @Lob
    @Column(nullable = false)
    private String yamlConfig;

    @Lob
    private byte[] templateBytes;
}
