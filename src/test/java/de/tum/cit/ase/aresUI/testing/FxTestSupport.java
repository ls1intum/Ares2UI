package de.tum.cit.ase.aresUI.testing;

import javafx.application.Platform;

import java.awt.GraphicsEnvironment;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility helpers for running JavaFX code during tests.
 *
 * <p>Description: Ensures the JavaFX toolkit is initialized and provides helpers for executing code on
 * the FX thread synchronously.
 *
 * <p>Design Rationale: Keeps JavaFX-specific boilerplate out of tests.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public final class FxTestSupport {

    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean TOOLKIT_AVAILABLE = new AtomicBoolean(true);

    private FxTestSupport() {
    }

    /**
     * Initializes the JavaFX toolkit once per JVM.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public static void ensureToolkit() {
        if (TOOLKIT_STARTED.compareAndSet(false, true)) {
            if (!Boolean.getBoolean("enable.javafx.tests")) {
                TOOLKIT_AVAILABLE.set(false);
                return;
            }
            if (GraphicsEnvironment.isHeadless()) {
                TOOLKIT_AVAILABLE.set(false);
                return;
            }
            System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/javafx-cache");
            System.setProperty("prism.order", "sw");
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException ignored) {
                // Toolkit already initialized.
                latch.countDown();
            } catch (Throwable throwable) {
                TOOLKIT_AVAILABLE.set(false);
                return;
            }
            await(latch);
        }
    }

    /**
     * @return true if the toolkit started successfully
     */
    public static boolean isToolkitAvailable() {
        return TOOLKIT_AVAILABLE.get();
    }

    /**
     * Runs a task on the FX thread and waits for completion.
     *
     * @param runnable code to run
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public static void runOnFx(Runnable runnable) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } finally {
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Executes a callable on the FX thread and returns its result.
     *
     * @param callable work to run
     * @return result of the callable
     * @param <T> result type
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public static <T> T callOnFx(Callable<T> callable) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<RuntimeException> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                error.set(new RuntimeException(e));
            } finally {
                latch.countDown();
            }
        });
        await(latch);
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX operation timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX", e);
        }
    }
}
