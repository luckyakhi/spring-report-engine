
package com.example.reporting.store;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportConfigRepository extends JpaRepository<ReportConfigEntity, Long> {
    Optional<ReportConfigEntity> findFirstByReportCodeAndVersionOrderByIdDesc(String code, String version);
}
