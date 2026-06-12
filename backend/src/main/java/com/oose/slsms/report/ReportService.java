package com.oose.slsms.report;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy Pattern — dispatcher.
 *
 * Spring injects every {@link ReportStrategy} bean as a list; this service
 * indexes them by {@link ReportStrategy#type()} and forwards
 * {@link #generate(String)} calls to the matching strategy. Callers (the
 * controller, tests) only depend on this class and the {@link ReportStrategy}
 * interface — adding a new report type requires no change here.
 */
@Service
public class ReportService {

    private final Map<String, ReportStrategy> strategiesByType;

    public ReportService(List<ReportStrategy> strategies) {
        this.strategiesByType = strategies.stream()
                .collect(Collectors.toMap(ReportStrategy::type, Function.identity()));
    }

    public Object generate(String type) {
        ReportStrategy strategy = strategiesByType.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unknown report type '" + type + "' (available: "
                            + String.join(", ", strategiesByType.keySet()) + ")");
        }
        return strategy.generate();
    }

    public List<ReportTypeDto> availableTypes() {
        return strategiesByType.values().stream()
                .map(s -> new ReportTypeDto(s.type(), s.title()))
                .sorted(Comparator.comparing(ReportTypeDto::type))
                .toList();
    }
}
