package de.tum.cit.ase.aresUI.policy.rules;

/**
 * A rule that specifies a timeout period.
 */
public class TimeoutRule {

    private final int timeoutSeconds;

    /**
     * Creates a timeout rule.
     *
     * @param timeoutSeconds timeout in seconds (must be positive)
     * @throws IllegalArgumentException if {@code timeoutSeconds} is not positive
     */
    public TimeoutRule(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be > 0");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Returns the timeout in seconds.
     *
     * @return timeout value
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
