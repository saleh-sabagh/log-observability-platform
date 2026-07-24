package rule;

import alert.AlertService;
import model.LogEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RuleEngine {

    private final List<RuleDefinition> rules;
    private final AlertService alertService;

    /**
     * Map key is composed of "RuleName::Component"
     * to keep separate sliding windows for each component dynamically.
     */
    private final Map<String, SlidingWindowCounter> counters =
            new ConcurrentHashMap<>();
    public RuleEngine(
            List<RuleDefinition> rules,
            AlertService alertService
    ) {
        this.rules = Objects.requireNonNull(rules, "rules cannot be null");
        this.alertService = Objects.requireNonNull(alertService, "alertService cannot be null");
    }

    /**
     * Evaluates a single incoming log event against all enabled rules.
     */
    public void evaluate(LogEvent event) {

        Objects.requireNonNull(event, "event cannot be null");

        for (RuleDefinition rule : rules) {

            if (!rule.isEnabled()) {
                continue;
            }

            if (!matchesComponent(rule, event)) {
                continue;
            }

            switch (rule.getRuleType()) {

                case ERROR ->
                        evaluateErrorRule(rule, event);

                case LEVEL_RATE ->
                        evaluateLevelRateRule(rule, event);

                case TOTAL_RATE ->
                        evaluateTotalRateRule(rule, event);
            }
        }
    }

    private void evaluateErrorRule(
            RuleDefinition rule,
            LogEvent event
    ) {

        if (!matchesLevel(rule, event)) {
            return;
        }

        alertService.createAlert(
                rule,
                event,
                List.of(event)
        );
    }

    private void evaluateLevelRateRule(
            RuleDefinition rule,
            LogEvent event
    ) {

        if (!matchesLevel(rule, event)) {
            return;
        }

        SlidingWindowCounter counter = getOrCreateCounter(rule, event.getComponent());

        counter.addEvent(event);

        if (counter.count() == rule.getThreshold()) {

            alertService.createAlert(
                    rule,
                    event,
                    counter.getLastEvents(rule.getMaxLastMessages())
            );
        }
    }

    private void evaluateTotalRateRule(
            RuleDefinition rule,
            LogEvent event
    ) {

        SlidingWindowCounter counter = getOrCreateCounter(rule, event.getComponent());

        counter.addEvent(event);

        if (counter.count() == rule.getThreshold()) {

            alertService.createAlert(
                    rule,
                    event,
                    counter.getLastEvents(rule.getMaxLastMessages())
            );
        }
    }

    /**
     * Creates or retrieves a specific SlidingWindowCounter for a Rule + Component combination.
     */
    private SlidingWindowCounter getOrCreateCounter(RuleDefinition rule, String component) {
        // ایجاد کلید ترکیبی: مثلا "System Overload::payment-service"
        String key = rule.getRuleName() + "::" + (component != null ? component : "unknown");

        return counters.computeIfAbsent(
                key,
                k -> new SlidingWindowCounter(rule.getTimeWindowSeconds())
        );
    }

    private boolean matchesComponent(
            RuleDefinition rule,
            LogEvent event
    ) {

        return rule.getComponent() == null
                || rule.getComponent().isBlank()
                || rule.getComponent().equalsIgnoreCase(event.getComponent());
    }

    private boolean matchesLevel(
            RuleDefinition rule,
            LogEvent event
    ) {

        return rule.getLogLevel() != null
                && rule.getLogLevel().equalsIgnoreCase(event.getLevel());
    }
}