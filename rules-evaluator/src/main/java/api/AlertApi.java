package api;

import alert.Alert;
import alert.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlertApi {

    private final AlertService alertService;
    private Javalin app;

    public AlertApi(AlertService alertService) {
        this.alertService = Objects.requireNonNull(alertService, "alertService cannot be null");
    }

    public void start(int port) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.app = Javalin.create(config -> {
            config.jsonMapper(new JsonMapper() {
                @NotNull
                @Override
                public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    try {
                        return mapper.writeValueAsString(obj);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize object", e);
                    }
                }

                @NotNull
                @Override
                public <T> T fromJsonString(@NotNull String json, @NotNull Type type) {
                    try {
                        return mapper.readValue(json, mapper.constructType(type));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize JSON", e);
                    }
                }
            });
        }).start(port);

        registerRoutes();
    }

    public void stop() {
        if (this.app != null) {
            this.app.stop();
        }
    }

    private void registerRoutes() {
        app.get("/alerts", ctx -> {
            List<AlertResponse> response = alertService.getAllAlerts()
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