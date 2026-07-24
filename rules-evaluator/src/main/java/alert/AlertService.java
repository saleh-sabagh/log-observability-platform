package alert;

import model.LogEvent;
import rule.RuleDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = Objects.requireNonNull(
                alertRepository,
                "alertRepository cannot be null"
        );
    }

    public void createAlert(
            RuleDefinition rule,
            LogEvent triggeringEvent,
            List<LogEvent> matchedEvents
    ) {

        Alert alert = Alert.builder()
                .ruleName(rule.getRuleName())
                .component(triggeringEvent.getComponent())
                .description(buildDescription(rule, matchedEvents))
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAllOrderedByCreatedAt();
    }

    private String buildDescription(
            RuleDefinition rule,
            List<LogEvent> matchedEvents
    ) {

        String messages = matchedEvents.stream()
                .limit(rule.getMaxLastMessages())
                .map(LogEvent::getMessage)
                .collect(Collectors.joining(" | "));

        return String.format(
                "Rule '%s' triggered with %d matching event(s). Recent messages: %s",
                rule.getRuleName(),
                matchedEvents.size(),
                messages
        );
    }
}