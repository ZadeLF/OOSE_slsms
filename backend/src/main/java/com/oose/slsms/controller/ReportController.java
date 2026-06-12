package com.oose.slsms.controller;

import com.oose.slsms.report.ReportService;
import com.oose.slsms.report.ReportTypeDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Lists available report types, for the frontend report picker. */
    @GetMapping
    public List<ReportTypeDto> listTypes() {
        return reportService.availableTypes();
    }

    /** Generates the report for the given type (400 if unknown). */
    @GetMapping("/{type}")
    public Object get(@PathVariable String type) {
        return reportService.generate(type);
    }
}
