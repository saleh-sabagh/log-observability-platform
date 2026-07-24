package rule;

import alert.AlertService;
import model.LogEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RuleEngine {

    private final List<RuleDefinition> rules;
    private final AlertService alertService;

    /**
     * One sliding window per rate-based rule.
     */
    private final Map<String, SlidingWindowCounter> counters = new HashMap<>();

    public RuleEngine(
            List<RuleDefinition> rules,
            AlertService alertService
    ) {
        this.rules = Objects.requireNonNull(rules, "rules cannot be null");
        this.alertService = Objects.requireNonNull(alertService, "alertService cannot be null");

        initializeCounters();
    }

    private void initializeCounters() {

        for (RuleDefinition rule : rules) {

            if (rule.getRuleType() != RuleType.ERROR) {
                counters.put(
                        rule.getRuleName(),
                        new SlidingWindowCounter(rule.getTimeWindowSeconds())
                );
            }
        }
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

        SlidingWindowCounter counter = counters.get(rule.getRuleName());

        counter.addEvent(event);

        if (counter.count() >= rule.getThreshold()) {

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

        SlidingWindowCounter counter = counters.get(rule.getRuleName());

        counter.addEvent(event);

        if (counter.count() >= rule.getThreshold()) {

            alertService.createAlert(
                    rule,
                    event,
                    counter.getLastEvents(rule.getMaxLastMessages())
            );
        }
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