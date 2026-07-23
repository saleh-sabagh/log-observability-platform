package serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LogEvent;
import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
public class LogEventSerializer implements Serializer<LogEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @Override
    public byte[] serialize(String topic, LogEvent data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize LogEvent", e);
        }
    }
}