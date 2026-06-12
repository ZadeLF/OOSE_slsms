package com.oose.slsms.report;

/**
 * Strategy Pattern — common interface for all usage/analysis reports.
 *
 * Each concrete strategy is a Spring-managed {@code @Component}; {@link ReportService}
 * collects all of them and dispatches by {@link #type()}. Adding a new report
 * type (e.g. a future occupancy-over-time report) only requires adding a new
 * implementation — no changes to {@link ReportService} or the controller.
 */
public interface ReportStrategy {

    /** Stable identifier used in the REST path, e.g. {@code GET /api/reports/{type}}. */
    String type();

    /** Human-readable title, shown in the frontend report picker. */
    String title();

    /** Computes the report from the system's current in-memory state. */
    Object generate();
}
