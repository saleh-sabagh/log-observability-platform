package rule;

public enum RuleType {
    /**
     * Trigger an alert immediately when a specific log level (e.g., ERROR) occurs.
     */
    ERROR,

    /**
     * Trigger an alert if the rate of a specific log level exceeds a threshold within a time window.
     */
    LEVEL_RATE,

    /**
     * Trigger an alert if the total rate of all logs for a component exceeds a threshold within a time window.
     */
    TOTAL_RATE
}