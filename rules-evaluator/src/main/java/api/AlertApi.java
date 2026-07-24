package api;

import alert.Alert;
import alert.AlertService;
import io.javalin.Javalin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlertApi {

    private final AlertService alertService;

    public AlertApi(AlertService alertService) {
        this.alertService = Objects.requireNonNull(
                alertService,
                "alertService cannot be null"
        );
    }

    public void registerRoutes(Javalin app) {

        app.get("/alerts", ctx -> {

            List<AlertResponse> response =
                    alertService.getAllAlerts()
                            .stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());

            ctx.json(response);
        });
    }

    private AlertResponse toResponse(Alert alert) {

        return AlertResponse.builder()
                .id(alert.getId())
                .ruleName(alert.getRuleName())
                .component(alert.getComponent())
                .description(alert.getDescription())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}