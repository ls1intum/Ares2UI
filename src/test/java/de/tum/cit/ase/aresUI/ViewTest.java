package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link View}.
 *
 * <p>Description: Validates observable wiring and status messaging without launching the full UI.
 *
 * <p>Design Rationale: Covers the JavaFX view logic in isolation using a test selection provider.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class ViewTest {

    @BeforeAll
    static void setupFx() {
        FxTestSupport.ensureToolkit();
        Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");
    }

    /**
     * Ensures project directory selections are emitted.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void projectDirectoryObservableEmitsSelectedFile() throws Exception {
        TestSelectionProvider provider = new TestSelectionProvider();
        View view = FxTestSupport.callOnFx(() -> new View(provider));
        provider.enqueueDirectory(new File("project"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<File> emitted = new AtomicReference<>();
        Disposable disposable = view.projectDirectoryObservable().subscribe(file -> {
            emitted.set(file);
            latch.countDown();
        });

        FxTestSupport.runOnFx(() -> view.getProjectButton().fire());

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(emitted.get().getPath()).endsWith("project");
        disposable.dispose();
    }

    /**
     * Ensures policy file selections are emitted.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void policyFileObservableEmitsSelectedFile() throws Exception {
        TestSelectionProvider provider = new TestSelectionProvider();
        View view = FxTestSupport.callOnFx(() -> new View(provider));
        provider.enqueuePolicy(new File("policy.yaml"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<File> emitted = new AtomicReference<>();
        Disposable disposable = view.policyFileObservable().subscribe(file -> {
            emitted.set(file);
            latch.countDown();
        });

        FxTestSupport.runOnFx(() -> view.getPolicyButton().fire());

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(emitted.get().getPath()).endsWith("policy.yaml");
        disposable.dispose();
    }

    /**
     * Ensures create/reset button observables emit action events.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void createAndResetObservablesEmitEvents() throws Exception {
        View view = FxTestSupport.callOnFx(() -> new View(new TestSelectionProvider()));

        CountDownLatch createLatch = new CountDownLatch(1);
        CountDownLatch resetLatch = new CountDownLatch(1);
        Disposable createDisposable = view.createFilesObservable().subscribe(e -> createLatch.countDown());
        Disposable resetDisposable = view.resetObservable().subscribe(e -> resetLatch.countDown());

        FxTestSupport.runOnFx(() -> {
            view.getCreateFilesButton().fire();
            view.getResetButton().fire();
        });

        assertThat(createLatch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(resetLatch.await(2, TimeUnit.SECONDS)).isTrue();
        createDisposable.dispose();
        resetDisposable.dispose();
    }

    /**
     * Validates helper methods that update UI controls.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void helperMethodsUpdateFieldsAndStatus() {
        View view = FxTestSupport.callOnFx(() -> new View(new TestSelectionProvider()));
        FxTestSupport.runOnFx(() -> new Scene(view.getGridPane()));

        FxTestSupport.runOnFx(() -> {
            view.updateProjectDirectory("dir");
            view.updatePolicyFile("policy");
            view.showStatus("ok");
            view.showError("bad");
        });

        String statusText = FxTestSupport.callOnFx(() -> view.getStatusTextArea().getText());
        assertThat(statusText).contains("[STATUS] ok").contains("[ERROR] bad");
        assertThat(FxTestSupport.callOnFx(() -> view.getProjectTextField().getText())).isEqualTo("dir");
        assertThat(FxTestSupport.callOnFx(() -> view.getPolicyTextField().getText())).isEqualTo("policy");

        FxTestSupport.runOnFx(view::clearStatus);
        assertThat(FxTestSupport.callOnFx(() -> view.getStatusTextArea().getText())).isEmpty();

        FxTestSupport.runOnFx(() -> {
            view.resetFields();
        });
        assertThat(FxTestSupport.callOnFx(() -> view.getProjectTextField().getText())).isEmpty();
        assertThat(FxTestSupport.callOnFx(() -> view.getPolicyTextField().getText())).isEmpty();
    }

    private static final class TestSelectionProvider implements SelectionProvider {
        private final Queue<Optional<File>> directories = new ArrayDeque<>();
        private final Queue<Optional<File>> policies = new ArrayDeque<>();

        void enqueueDirectory(File file) {
            directories.add(Optional.of(file));
        }

        void enqueuePolicy(File file) {
            policies.add(Optional.of(file));
        }

        @Override
        public Optional<File> selectProjectDirectory(javafx.stage.Window owner) {
            return directories.isEmpty() ? Optional.empty() : directories.remove();
        }

        @Override
        public Optional<File> selectPolicyFile(javafx.stage.Window owner) {
            return policies.isEmpty() ? Optional.empty() : policies.remove();
        }
    }
}
