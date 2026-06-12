package com.oose.slsms.report;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReportService} — the Strategy Pattern dispatcher.
 * Verifies that strategies are interchangeable (polymorphism) and that
 * an unknown report type is rejected.
 */
class ReportServiceTest {

    /** Minimal fake strategy used to test dispatch without real domain data. */
    static class FakeStrategy implements ReportStrategy {
        private final String type;
        private final String title;
        private final Object payload;
        boolean called = false;

        FakeStrategy(String type, String title, Object payload) {
            this.type = type;
            this.title = title;
            this.payload = payload;
        }

        @Override public String type() { return type; }
        @Override public String title() { return title; }

        @Override
        public Object generate() {
            called = true;
            return payload;
        }
    }

    @Test
    void dispatches_to_the_matching_strategy_only() {
        FakeStrategy strategyA = new FakeStrategy("type-a", "Report A", "result-a");
        FakeStrategy strategyB = new FakeStrategy("type-b", "Report B", "result-b");
        ReportService service = new ReportService(List.of(strategyA, strategyB));

        Object result = service.generate("type-b");

        assertEquals("result-b", result);
        assertTrue(strategyB.called);
        assertFalse(strategyA.called);
    }

    @Test
    void strategies_are_interchangeable_by_type() {
        FakeStrategy strategyA = new FakeStrategy("type-a", "Report A", "result-a");
        FakeStrategy strategyB = new FakeStrategy("type-b", "Report B", "result-b");
        ReportService service = new ReportService(List.of(strategyA, strategyB));

        assertEquals("result-a", service.generate("type-a"));
        assertEquals("result-b", service.generate("type-b"));
    }

    @Test
    void unknown_type_throws_illegal_argument_exception() {
        ReportService service = new ReportService(List.of(new FakeStrategy("type-a", "Report A", "result-a")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.generate("does-not-exist"));
        assertTrue(ex.getMessage().contains("does-not-exist"));
    }

    @Test
    void available_types_lists_all_registered_strategies_sorted_by_type() {
        FakeStrategy strategyA = new FakeStrategy("type-b", "Report B", "result-b");
        FakeStrategy strategyB = new FakeStrategy("type-a", "Report A", "result-a");
        ReportService service = new ReportService(List.of(strategyA, strategyB));

        List<ReportTypeDto> types = service.availableTypes();

        assertEquals(2, types.size());
        assertEquals("type-a", types.get(0).type());
        assertEquals("type-b", types.get(1).type());
    }
}
