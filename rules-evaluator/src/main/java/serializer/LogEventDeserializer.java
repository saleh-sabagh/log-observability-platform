package serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.LogEvent;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class LogEventDeserializer implements Deserializer<LogEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public LogEvent deserialize(String topic, byte[] data) {

        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(data, LogEvent.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialize LogEvent.", e);
        }
    }
}