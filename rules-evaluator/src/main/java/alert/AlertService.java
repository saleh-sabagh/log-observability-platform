package alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = Objects.requireNonNull(
                alertRepository,
                "alertRepository cannot be null"
        );
    }

    public void createAlert(
            String ruleName,
            String component,
            String description
    ) {

        Alert alert = Alert.builder()
                .ruleName(ruleName)
                .component(component)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAllOrderedByCreatedAt();
    }
}