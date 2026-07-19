package publisher;

import model.LogEvent;

public interface LogPublisher {
    void publish(LogEvent event) throws Exception;
}