package de.tum.cit.ase.aresUI.testing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executor that runs tasks synchronously on the calling thread.
 *
 * <p>Description: Simplifies testing asynchronous code paths by avoiding background threads.
 *
 * <p>Design Rationale: Provides deterministic execution for ViewModel tests.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class DirectExecutorService extends AbstractExecutorService {
    /**
     * Tracks shutdown status.
     */
    private volatile boolean shutdown;

    /**
     * Marks the executor as shut down.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public void shutdown() {
        shutdown = true;
    }

    /**
     * Immediately marks shutdown and returns no pending tasks.
     *
     * @return empty list of pending runnables
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return Collections.emptyList();
    }

    /**
     * Indicates if shutdown has been initiated.
     *
     * @return true if shut down
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Indicates if termination has completed.
     *
     * @return true if shut down
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    /**
     * Waits for termination (no-op since synchronous).
     *
     * @return shutdown state
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return shutdown;
    }

    /**
     * Executes the command immediately on the calling thread.
     *
     * @param command runnable to execute
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
