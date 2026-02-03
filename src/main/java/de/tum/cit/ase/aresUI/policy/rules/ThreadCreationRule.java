package de.tum.cit.ase.aresUI.policy.rules;

/**
 * Rule for thread creation specifying the number of threads and the thread class.
 */
public class ThreadCreationRule {

    private final int numberOfThreads;
    private final String threadClass;

    /**
     * Creates a thread creation rule.
     *
     * @param numberOfThreads number of threads that may be created (must be non-negative)
     * @param threadClass fully qualified class name of the thread type
     * @throws IllegalArgumentException if {@code numberOfThreads} is negative or {@code threadClass} is blank
     */
    public ThreadCreationRule(int numberOfThreads, String threadClass) {
        if (numberOfThreads < 0) {
            throw new IllegalArgumentException("numberOfThreads must be >= 0");
        }
        if (threadClass == null || threadClass.trim().isEmpty()) {
            throw new IllegalArgumentException("threadClass must not be blank");
        }
        this.numberOfThreads = numberOfThreads;
        this.threadClass = threadClass.trim();
    }

    /**
     * Returns the number of threads.
     *
     * @return number of threads
     */
    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    /**
     * Returns the thread class name.
     *
     * @return thread class
     */
    public String getThreadClass() {
        return threadClass;
    }
}
