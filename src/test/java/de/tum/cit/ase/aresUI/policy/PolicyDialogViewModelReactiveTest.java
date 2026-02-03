package de.tum.cit.ase.aresUI.policy;

import de.tum.cit.ase.aresUI.SelectionProvider;
import de.tum.cit.ase.aresUI.policy.rules.*;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogView;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogViewModel;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link PolicyDialogViewModel}.
 *
 * <p>Style mirrors {@code ViewModelTest}: the view is mocked and driven via RxJava {@link PublishSubject}s.
 */
class PolicyDialogViewModelReactiveTest {

    @Test
    void initializeSubscribesAndHandlesObservableErrors() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> {
                },
                p -> false
        );

        vm.initialize();

        saveSubject.onError(new RuntimeException("save error"));
        cancelSubject.onError(new RuntimeException("cancel error"));

        verify(view, times(2)).setError(startsWith("Unexpected error: "));
    }

    @Test
    void saveSuccessInvokesOnSaveAndCloses() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        // Valid supervised code inputs
        when(view.getSelectedConfig()).thenReturn("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        when(view.getRootPackage()).thenReturn("de.example");
        when(view.getMainClass()).thenReturn("Main");
        when(view.getTestClassesRaw()).thenReturn("de.example.ExampleTest\n");

        // Valid rules (empty lists are fine)
        when(view.getFileSystemRules()).thenReturn(List.of());
        when(view.getNetworkConnectionRules()).thenReturn(List.of());
        when(view.getCommandExecutionRules()).thenReturn(List.of());
        when(view.getThreadCreationRules()).thenReturn(List.of());
        when(view.getPackageImportRules()).thenReturn(List.of());
        when(view.getTimeoutRules()).thenReturn(List.of());

        Path target = Path.of("out", "security-policy.yaml").toAbsolutePath();
        when(selectionProvider.selectSavePolicyFile(any(), eq("security-policy.yaml")))
                .thenReturn(Optional.of(target.toFile()));

        AtomicBoolean saved = new AtomicBoolean(false);
        AtomicReference<PolicyDialogModel> capturedModel = new AtomicReference<>();
        AtomicReference<Path> capturedPath = new AtomicReference<>();

        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> {
                    saved.set(true);
                    capturedModel.set(m);
                    capturedPath.set(p);
                },
                p -> false
        );
        vm.initialize();

        saveSubject.onNext(new ActionEvent());

        assertThat(saved).isTrue();
        assertThat(capturedPath.get()).isEqualTo(target);
        assertThat(capturedModel.get().getRootPackage()).isEqualTo("de.example");

        verify(view).clearError();
        verify(view).close();
        verify(view, never()).setError(anyString());
    }

    @Test
    void saveMissingInputsShowsErrorAndDoesNotInvokeOnSave() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        // Missing config
        when(view.getSelectedConfig()).thenReturn(" ");
        when(view.getRootPackage()).thenReturn("de.example");
        when(view.getMainClass()).thenReturn("Main");
        when(view.getTestClassesRaw()).thenReturn("de.example.ExampleTest");

        when(view.getFileSystemRules()).thenReturn(List.of());
        when(view.getNetworkConnectionRules()).thenReturn(List.of());
        when(view.getCommandExecutionRules()).thenReturn(List.of());
        when(view.getThreadCreationRules()).thenReturn(List.of());
        when(view.getPackageImportRules()).thenReturn(List.of());
        when(view.getTimeoutRules()).thenReturn(List.of());

        AtomicBoolean saved = new AtomicBoolean(false);
        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> saved.set(true),
                p -> false
        );
        vm.initialize();

        saveSubject.onNext(new ActionEvent());

        assertThat(saved).isFalse();
        verify(view).setError("Please select a programming language configuration.");
        verify(view, never()).close();
        verify(selectionProvider, never()).selectSavePolicyFile(any(), anyString());
    }

    @Test
    void saveSelectionCanceledDoesNotInvokeOnSaveOrClose() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        // Valid supervised code inputs
        when(view.getSelectedConfig()).thenReturn("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        when(view.getRootPackage()).thenReturn("de.example");
        when(view.getMainClass()).thenReturn("Main");
        when(view.getTestClassesRaw()).thenReturn("de.example.ExampleTest");

        when(view.getFileSystemRules()).thenReturn(List.of());
        when(view.getNetworkConnectionRules()).thenReturn(List.of());
        when(view.getCommandExecutionRules()).thenReturn(List.of());
        when(view.getThreadCreationRules()).thenReturn(List.of());
        when(view.getPackageImportRules()).thenReturn(List.of());
        when(view.getTimeoutRules()).thenReturn(List.of());

        when(selectionProvider.selectSavePolicyFile(any(), anyString())).thenReturn(Optional.empty());

        AtomicBoolean saved = new AtomicBoolean(false);
        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> saved.set(true),
                p -> false
        );
        vm.initialize();

        saveSubject.onNext(new ActionEvent());

        assertThat(saved).isFalse();
        verify(view, never()).close();
        verify(view, never()).setError(anyString());
    }

    @Test
    void saveUnsafeLocationShowsErrorAndDoesNotInvokeOnSave() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        // Valid supervised code inputs
        when(view.getSelectedConfig()).thenReturn("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        when(view.getRootPackage()).thenReturn("de.example");
        when(view.getMainClass()).thenReturn("Main");
        when(view.getTestClassesRaw()).thenReturn("de.example.ExampleTest");

        when(view.getFileSystemRules()).thenReturn(List.of());
        when(view.getNetworkConnectionRules()).thenReturn(List.of());
        when(view.getCommandExecutionRules()).thenReturn(List.of());
        when(view.getThreadCreationRules()).thenReturn(List.of());
        when(view.getPackageImportRules()).thenReturn(List.of());
        when(view.getTimeoutRules()).thenReturn(List.of());

        Path target = Path.of("project", "security-policy.yaml").toAbsolutePath();
        when(selectionProvider.selectSavePolicyFile(any(), anyString()))
                .thenReturn(Optional.of(target.toFile()));

        AtomicBoolean saved = new AtomicBoolean(false);
        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> saved.set(true),
                p -> true
        );
        vm.initialize();

        saveSubject.onNext(new ActionEvent());

        assertThat(saved).isFalse();
        verify(view).setError(startsWith("Refusing to save inside the selected project folder"));
        verify(view, never()).close();
    }

    @Test
    void cancelClosesDialog() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> {
                },
                p -> false
        );
        vm.initialize();

        cancelSubject.onNext(new ActionEvent());

        verify(view).close();
    }

    @Test
    void showCallsViewShowAndDisposesSubscriptions() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> {
                },
                p -> false
        );

        vm.show();

        verify(view).show();

        // After show() returns, disposables are disposed. Emissions should not close the dialog.
        saveSubject.onNext(new ActionEvent());
        cancelSubject.onNext(new ActionEvent());

        verify(view, never()).close();
    }

    @Test
    void buildModelValidationErrorsSurfaceAsViewError() {
        PolicyDialogView view = mock(PolicyDialogView.class);
        SelectionProvider selectionProvider = mock(SelectionProvider.class);

        PublishSubject<ActionEvent> saveSubject = PublishSubject.create();
        PublishSubject<ActionEvent> cancelSubject = PublishSubject.create();
        when(view.saveObservable()).thenReturn(saveSubject);
        when(view.cancelObservable()).thenReturn(cancelSubject);

        // Valid supervised code inputs
        when(view.getSelectedConfig()).thenReturn("JAVA_USING_MAVEN_WALA_AND_ASPECTJ");
        when(view.getRootPackage()).thenReturn("de.example");
        when(view.getMainClass()).thenReturn("Main");
        when(view.getTestClassesRaw()).thenReturn("de.example.ExampleTest");

        // Keep rule objects valid, but trigger view-model validation failures.
        when(view.getFileSystemRules()).thenReturn(List.of());
        when(view.getNetworkConnectionRules()).thenReturn(List.of(new NetworkConnectionRule("example.com", 443, true, true, true)));
        when(view.getCommandExecutionRules()).thenReturn(List.of());
        when(view.getThreadCreationRules()).thenReturn(List.of());
        when(view.getPackageImportRules()).thenReturn(List.of());
        when(view.getTimeoutRules()).thenReturn(List.of(new TimeoutRule(1), new TimeoutRule(2)));

        AtomicBoolean saved = new AtomicBoolean(false);
        PolicyDialogViewModel vm = new PolicyDialogViewModel(
                view,
                selectionProvider,
                null,
                (m, p) -> saved.set(true),
                p -> false
        );
        vm.initialize();

        saveSubject.onNext(new ActionEvent());

        assertThat(saved).isFalse();
        verify(view).setError("Please provide only one timeout entry (or remove all of them).");
        verify(selectionProvider, never()).selectSavePolicyFile(any(), anyString());
    }
}
