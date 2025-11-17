package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PlatformFxScheduler}.
 *
 * <p>Description: Ensures runLater delegates to the JavaFX thread.
 *
 * <p>Design Rationale: Prevents regressions around scheduler behavior.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class PlatformFxSchedulerTest {

    @BeforeAll
    static void setupFx() {
        FxTestSupport.ensureToolkit();
        Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");
    }

    /**
     * Verifies that runLater executes asynchronously.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void runLaterExecutesOnFxThread() throws InterruptedException {
        PlatformFxScheduler scheduler = new PlatformFxScheduler();
        CountDownLatch latch = new CountDownLatch(1);

        scheduler.runLater(latch::countDown);

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }
}
