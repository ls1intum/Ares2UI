package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.generation.AresTestGenerator;
import de.tum.cit.ase.aresUI.testing.DirectExecutorService;
import de.tum.cit.ase.aresUI.testing.FxTestSupport;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link ViewModel}.
 *
 * <p>Description: Validates reactive subscriptions, generation flow, and error handling.
 *
 * <p>Design Rationale: Ensures the MVVM controller behaves deterministically under various scenarios.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
class ViewModelTest {

    /**
     * Verifies selections update model and trigger UI updates.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void initializeUpdatesModelOnSelections() {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        DirectExecutorService executorService = new DirectExecutorService();
        FxScheduler scheduler = Runnable::run;

        PublishSubject<File> directorySubject = PublishSubject.create();
        PublishSubject<File> policySubject = PublishSubject.create();
        PublishSubject<ActionEvent> createSubject = PublishSubject.create();
        PublishSubject<ActionEvent> resetSubject = PublishSubject.create();

        when(view.projectDirectoryObservable()).thenReturn(directorySubject);
        when(view.policyFileObservable()).thenReturn(policySubject);
        when(view.createFilesObservable()).thenReturn(createSubject);
        when(view.resetObservable()).thenReturn(resetSubject);

        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        doAnswer(invocation -> {
            errors.add(invocation.getArgument(0));
            return null;
        }).when(view).showError(anyString());

        ViewModel viewModel = new ViewModel(view, model, generator, executorService, scheduler);
        viewModel.initialize();

        File projectSelection = new File("projectDir");
        File policySelection = new File("policy.yaml");
        directorySubject.onNext(projectSelection);
        policySubject.onNext(policySelection);

        String expectedProject = projectSelection.getAbsolutePath();
        String expectedPolicy = policySelection.getAbsolutePath();
        assertThat(model.getProjectDirectory()).isEqualTo(expectedProject);
        assertThat(model.getPolicyFile()).isEqualTo(expectedPolicy);
        verify(view).updateProjectDirectory(expectedProject);
        verify(view).updatePolicyFile(expectedPolicy);

        directorySubject.onError(new RuntimeException("dir error"));
        policySubject.onError(new RuntimeException("policy error"));
        createSubject.onError(new RuntimeException("create error"));
        resetSubject.onError(new RuntimeException("reset error"));

        assertEquals(4, errors.size());
    }

    /**
     * Ensures successful generation clears status and invokes generator.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void executeCreateFilesRunsGeneratorOnSuccess() throws Exception {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        DirectExecutorService executorService = new DirectExecutorService();
        FxScheduler scheduler = Runnable::run;

        PublishSubject<File> directorySubject = PublishSubject.create();
        PublishSubject<File> policySubject = PublishSubject.create();
        PublishSubject<ActionEvent> createSubject = PublishSubject.create();
        PublishSubject<ActionEvent> resetSubject = PublishSubject.create();

        when(view.projectDirectoryObservable()).thenReturn(directorySubject);
        when(view.policyFileObservable()).thenReturn(policySubject);
        when(view.createFilesObservable()).thenReturn(createSubject);
        when(view.resetObservable()).thenReturn(resetSubject);

        List<String> statusMessages = Collections.synchronizedList(new ArrayList<>());
        doAnswer(invocation -> {
            statusMessages.add(invocation.getArgument(0));
            return null;
        }).when(view).showStatus(anyString());

        AtomicBoolean invoked = new AtomicBoolean(false);
        doAnswer(invocation -> {
            invoked.set(true);
            return null;
        }).when(generator).generateTests(any(), any(), any());

        ViewModel viewModel = new ViewModel(view, model, generator, executorService, scheduler);
        viewModel.initialize();

        Path projectPath = Path.of("project").toAbsolutePath();
        Path policyPath = Path.of("policy.yaml").toAbsolutePath();
        model.setProjectDirectory(projectPath.toString());
        model.setPolicyFile(policyPath.toString());

        viewModel.executeCreateFiles();

        verify(view).clearStatus();
        assertTrue(invoked.get(), "Generator should have been invoked");
        verify(generator).generateTests(policyPath, projectPath,
                projectPath.resolve("src").resolve("test").resolve("java"));

        assertFalse(statusMessages.isEmpty());
    }

    /**
     * Verifies missing selections result in an error message.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void executeCreateFilesShowsErrorWhenInputsMissing() throws Exception {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        DirectExecutorService executorService = new DirectExecutorService();
        FxScheduler scheduler = Runnable::run;

        PublishSubject<File> directorySubject = PublishSubject.create();
        PublishSubject<File> policySubject = PublishSubject.create();
        PublishSubject<ActionEvent> createSubject = PublishSubject.create();
        PublishSubject<ActionEvent> resetSubject = PublishSubject.create();

        when(view.projectDirectoryObservable()).thenReturn(directorySubject);
        when(view.policyFileObservable()).thenReturn(policySubject);
        when(view.createFilesObservable()).thenReturn(createSubject);
        when(view.resetObservable()).thenReturn(resetSubject);

        ViewModel viewModel = new ViewModel(view, model, generator, executorService, scheduler);
        viewModel.initialize();

        viewModel.executeCreateFiles();

        verify(view).showError("Please select both a project directory and a policy file.");
        verify(generator, never()).generateTests(any(), any(), any());
    }

    /**
     * Ensures generator failures surface in the UI.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void executeCreateFilesShowsErrorOnFailure() throws Exception {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        DirectExecutorService executorService = new DirectExecutorService();
        FxScheduler scheduler = Runnable::run;

        PublishSubject<File> directorySubject = PublishSubject.create();
        PublishSubject<File> policySubject = PublishSubject.create();
        PublishSubject<ActionEvent> createSubject = PublishSubject.create();
        PublishSubject<ActionEvent> resetSubject = PublishSubject.create();

        when(view.projectDirectoryObservable()).thenReturn(directorySubject);
        when(view.policyFileObservable()).thenReturn(policySubject);
        when(view.createFilesObservable()).thenReturn(createSubject);
        when(view.resetObservable()).thenReturn(resetSubject);

        AtomicBoolean invoked = new AtomicBoolean(false);
        doAnswer(invocation -> {
            invoked.set(true);
            throw new IOException("boom");
        }).when(generator).generateTests(any(), any(), any());

        ViewModel viewModel = new ViewModel(view, model, generator, executorService, scheduler);
        viewModel.initialize();

        Path projectPath = Path.of("project").toAbsolutePath();
        Path policyPath = Path.of("policy.yaml").toAbsolutePath();
        model.setProjectDirectory(projectPath.toString());
        model.setPolicyFile(policyPath.toString());

        viewModel.executeCreateFiles();

        assertTrue(invoked.get(), "Generator should have been invoked");
        verify(view).showError(startsWith("Error creating files: "));
    }

    /**
     * Verifies reset clears model and view state.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void resetClearsModelAndFields() {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        DirectExecutorService executorService = new DirectExecutorService();
        FxScheduler scheduler = Runnable::run;

        PublishSubject<File> directorySubject = PublishSubject.create();
        PublishSubject<File> policySubject = PublishSubject.create();
        PublishSubject<ActionEvent> createSubject = PublishSubject.create();
        PublishSubject<ActionEvent> resetSubject = PublishSubject.create();

        when(view.projectDirectoryObservable()).thenReturn(directorySubject);
        when(view.policyFileObservable()).thenReturn(policySubject);
        when(view.createFilesObservable()).thenReturn(createSubject);
        when(view.resetObservable()).thenReturn(resetSubject);

        ViewModel viewModel = new ViewModel(view, model, generator, executorService, scheduler);
        viewModel.initialize();

        model.setProjectDirectory("dir");
        model.setPolicyFile("policy");
        resetSubject.onNext(new ActionEvent());

        assertNull(model.getProjectDirectory());
        assertNull(model.getPolicyFile());
        verify(view).resetFields();
    }

    /**
     * Confirms stop disposes of resources.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void stopDisposesResources() {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        ExecutorService executor = mock(ExecutorService.class);
        FxScheduler scheduler = Runnable::run;

        ViewModel viewModel = new ViewModel(view, model, generator, executor, scheduler);
        viewModel.stop();

        verify(executor).shutdownNow();
    }

    /**
     * Ensures canCreateFiles relies on both selections being present.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void canCreateFilesValidatesModel() {
        View view = mock(View.class);
        Model model = new Model();
        AresTestGenerator generator = mock(AresTestGenerator.class);
        ExecutorService executor = mock(ExecutorService.class);
        FxScheduler scheduler = Runnable::run;

        ViewModel viewModel = new ViewModel(view, model, generator, executor, scheduler);

        assertFalse(viewModel.canCreateFiles());
        model.setProjectDirectory("dir");
        assertFalse(viewModel.canCreateFiles());
        model.setPolicyFile("policy");
        assertTrue(viewModel.canCreateFiles());
    }

    /**
     * Ensures formatThrowable handles nulls and missing messages.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void formatThrowableHandlesNullValues() throws Exception {
        View view = mock(View.class);
        ViewModel viewModel = new ViewModel(view, new Model(), mock(AresTestGenerator.class),
                new DirectExecutorService(), Runnable::run);

        var method = ViewModel.class.getDeclaredMethod("formatThrowable", Throwable.class);
        method.setAccessible(true);

        assertThat((String) method.invoke(viewModel, (Object) null)).isEqualTo("Unknown error");
        assertThat((String) method.invoke(viewModel, new RuntimeException())).isEqualTo("RuntimeException");
        assertThat((String) method.invoke(viewModel, new RuntimeException("boom"))).isEqualTo("boom");
    }

    /**
     * Covers the JavaFX start lifecycle to ensure initialization occurs.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Test
    void startInitializesScene() {
        FxTestSupport.ensureToolkit();
        org.junit.jupiter.api.Assumptions.assumeTrue(FxTestSupport.isToolkitAvailable(), "JavaFX toolkit unavailable");
        View view = mock(View.class);
        when(view.projectDirectoryObservable()).thenReturn(PublishSubject.create());
        when(view.policyFileObservable()).thenReturn(PublishSubject.create());
        when(view.createFilesObservable()).thenReturn(PublishSubject.create());
        when(view.resetObservable()).thenReturn(PublishSubject.create());
        when(view.getGridPane()).thenReturn(new GridPane());

        ExecutorService executor = mock(ExecutorService.class);
        ViewModel viewModel = new ViewModel(view, new Model(), mock(AresTestGenerator.class),
                executor, Runnable::run);

        FxTestSupport.runOnFx(() -> {
            try {
                viewModel.start(new javafx.stage.Stage());
                viewModel.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(view).getGridPane();
    }
}
