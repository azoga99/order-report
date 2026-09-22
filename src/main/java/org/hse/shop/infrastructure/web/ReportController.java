package org.hse.shop.infrastructure.web;

import org.hse.shop.application.SalesReport;
import org.hse.shop.application.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Отчёты для магазина */
@RestController
public class ReportController {

    private final SalesReportService reports;

    public ReportController(SalesReportService reports) {
        this.reports = reports;
    }

    /** Продажи за период, даты в формате 2026-09-01, обе границы включительно */
    @GetMapping("/api/reports/sales")
    public SalesReport sales(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reports.build(from, to);
    }
}
