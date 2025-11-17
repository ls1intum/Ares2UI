package de.tum.cit.ase.aresUI;

/**
 * Lightweight abstraction for scheduling work on the JavaFX application thread.
 *
 * <p>Description: Wrapping the JavaFX {@code Platform.runLater} call inside this interface allows the
 * controller logic to inject custom schedulers during testing or alternative environments.
 *
 * <p>Design Rationale: Decoupling the UI scheduler improves testability and keeps the ViewModel agnostic of
 * the concrete threading mechanism.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public interface FxScheduler {

    /**
     * Executes the supplied runnable on the UI scheduler.
     *
     * @param runnable the work that should run on the JavaFX thread
     * @since 0.0.1
     * @author Markus Paulsen
     */
    void runLater(Runnable runnable);
}
