package alert;

import java.util.List;

public interface AlertRepository {

    /**
     * Saves a new alert into the database.
     *
     * @param alert the alert object to save
     */
    void save(Alert alert);

    /**
     * Retrieves all stored alerts, typically ordered by creation time.
     *
     * @return a list of all alerts
     */
    List<Alert> findAllOrderedByTime();
}