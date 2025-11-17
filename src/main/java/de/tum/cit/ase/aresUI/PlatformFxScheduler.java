package de.tum.cit.ase.aresUI;

import javafx.application.Platform;

/**
 * Default scheduler that forwards directly to JavaFX's {@link Platform#runLater(Runnable)}.
 *
 * <p>Description: This implementation simply bridges the {@link FxScheduler} abstraction to the JavaFX
 * runtime so production code keeps using the official mechanism without any overhead.
 *
 * <p>Design Rationale: Providing a concrete implementation keeps the {@link FxScheduler} injectable while
 * ensuring a single location encapsulates JavaFX specifics.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class PlatformFxScheduler implements FxScheduler {

    /**
     * Schedules the runnable via {@link Platform#runLater(Runnable)}.
     *
     * @param runnable the work to be executed on the JavaFX thread
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public void runLater(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
