package de.tum.cit.ase.aresUI;

import de.tum.cit.ase.aresUI.generation.AresTestGenerator;
import de.tum.cit.ase.aresUI.generation.DefaultSecurityPolicyGeneratorFactory;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogViewContract;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogViewImpl;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogViewModel;
import de.tum.cit.ase.aresUI.policy.yaml.PolicyYamlCreator;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import de.tum.cit.ase.aresUI.policy.yaml.PolicyYamlParser;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX application controller that wires {@link View} events to business logic.
 *
 * <p>Description: Coordinates user interactions, validates selections, and delegates to the Ares generator.
 *
 * <p>Design Rationale: Implements the MVVM ViewModel layer to keep UI rendering and business logic separated.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class ViewModel extends Application {
    /**
     * Primary view containing all UI controls.
     */
    private final View view;
    /**
     * Mutable model storing user selections.
     */
    private final Model model;
    /**
     * Collects RxJava subscriptions to dispose them together during shutdown.
     */
    private final CompositeDisposable disposables = new CompositeDisposable();
    /**
     * Coordinates the invocation of the Ares generator.
     */
    private final AresTestGenerator generator;
    /**
     * Single-threaded executor used to run background tasks.
     */
    private final ExecutorService executorService;
    /**
     * Schedules UI updates on the JavaFX thread.
     */
    private final FxScheduler fxScheduler;
    private final PolicyYamlCreator policyYamlCreator;


    /**
     * Creates the production controller using real UI components and generator.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public ViewModel() {
        this(new View(),
                new Model(),
                new AresTestGenerator(new DefaultSecurityPolicyGeneratorFactory()),
                Executors.newSingleThreadExecutor(r -> {
                    Thread thread = new Thread(r, "ares-generator");
                    thread.setDaemon(true);
                    return thread;
                }),
                new PlatformFxScheduler(),
                new PolicyYamlCreator());
    }

    /**
     * Visible-for-testing constructor that allows injecting collaborators.
     *
     * @param view the UI view
     * @param model backing model
     * @param generator generator to execute
     * @param executorService executor running background tasks
     * @param fxScheduler scheduler responsible for UI updates
     * @since 0.0.1
     * @author Markus Paulsen
     */
    ViewModel(View view, Model model, AresTestGenerator generator, ExecutorService executorService, FxScheduler fxScheduler, PolicyYamlCreator policyYamlCreator) {
        this.view = Objects.requireNonNull(view, "view");
        this.model = Objects.requireNonNull(model, "model");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.executorService = Objects.requireNonNull(executorService, "executorService");
        this.fxScheduler = Objects.requireNonNull(fxScheduler, "fxScheduler");
        this.policyYamlCreator = Objects.requireNonNull(policyYamlCreator, "policyYamlCreator");
    }

    /**
     * Subscribes to all UI events exposed by the {@link View}.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void initialize() {
        disposables.add(view.projectDirectoryObservable()
                .map(File::getAbsolutePath)
                .subscribe(this::onDirectorySelected, this::onDirectoryError)
        );

        disposables.add(view.policyFileObservable()
                .map(File::getAbsolutePath)
                .subscribe(this::onPolicyFileSelected, this::onPolicyFileError)
        );

        disposables.add(view.createFilesObservable()
                .subscribe(this::onCreateFiles, this::onCreateFilesError)
        );

        disposables.add(view.resetObservable()
                .subscribe(this::onReset, this::onResetError)
        );

        disposables.add(view.createPolicyObservable()
                .subscribe(__ -> onOpenCreateOrEditPolicyDialog(), this::onCreatePolicyError)
        );
    }

    /**
     * Updates the model and UI once a directory was picked.
     *
     * @param directory absolute directory path
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onDirectorySelected(String directory) {
        fxScheduler.runLater(() -> {
            model.setProjectDirectory(directory);
            view.updateProjectDirectory(directory);
        });

    }

    /**
     * Displays the directory selection error message.
     *
     * @param throwable root cause
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onDirectoryError(Throwable throwable) {
        fxScheduler.runLater(() -> view.showError("Error selecting project directory: " + throwable.getMessage()));
    }

    /**
     * Updates the model and UI once a policy file was picked.
     *
     * @param file absolute path to the policy file
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onPolicyFileSelected(String file) {
        fxScheduler.runLater(() -> {
            model.setPolicyFile(file);
            view.updatePolicyFile(file);

            // Only flip the button label; the user opens the dialog explicitly.
            view.setPolicyActionIsEdit(file != null && !file.isBlank());
        });
    }

    /**
     * Displays policy selection errors.
     *
     * @param throwable root cause
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onPolicyFileError(Throwable throwable) {
        fxScheduler.runLater(() -> view.showError("Error selecting policy file: " + throwable.getMessage()));
    }

    /**
     * Triggers generation on the JavaFX thread while immediately delegating to a background worker.
     *
     * @param actionEvent originating event
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onCreateFiles(ActionEvent actionEvent) {
        fxScheduler.runLater(this::executeCreateFiles);
    }

    /**
     * Validates inputs, runs the generator, and reports success or failure.
     * Package-private for testing.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    void executeCreateFiles() {
        view.clearStatus();
        if (!canCreateFiles()) {
            view.showError("Please select both a project directory and a policy file.");
            return;
        }

        Path policyPath = Path.of(model.getPolicyFile());
        Path projectPath = Path.of(model.getProjectDirectory());
        Path outputDirectory = projectPath.resolve("src").resolve("test").resolve("java");

        Runnable generationTask = () -> {
            try {
                generator.generateTests(policyPath, projectPath, outputDirectory);
                fxScheduler.runLater(() -> view.showStatus("Tests created successfully at " + outputDirectory));
            } catch (Exception exception) {
                fxScheduler.runLater(() -> view.showError("Error creating files: " + formatThrowable(exception)));
            }
        };
        executorService.submit(generationTask);
    }

    /**
     * Opens the policy dialog to create a new policy or edit the currently selected policy.
     *
     * <p>This method creates a JavaFX dialog view and its corresponding view model, optionally preloads
     * the dialog from the currently selected policy file, and then shows the dialog. All UI work is
     * scheduled on the JavaFX thread.
     *
     * @throws IllegalStateException if JavaFX operations are invoked when the toolkit is not initialized
     */
    private void onOpenCreateOrEditPolicyDialog() {
        fxScheduler.runLater(() -> {
            PolicyDialogViewContract dialogView = new PolicyDialogViewImpl(view.getWindow());

            // If a valid policy file is selected, preload it (edit mode).
            PolicyDialogModel imported = tryParseSelectedPolicyForEditing();
            if (imported != null) {
                dialogView.loadFromModel(imported);
            }

            PolicyDialogViewModel dialogVm = new PolicyDialogViewModel(
                    dialogView,
                    new DefaultSelectionProvider(),
                    view.getWindow(),
                    (policyModel, outputPath) -> executorService.submit(() -> {
                        try {
                            Path policyPath = policyYamlCreator.createPolicyYamlAt(outputPath, policyModel);

                            fxScheduler.runLater(() -> {
                                model.setPolicyFile(policyPath.toString());
                                view.updatePolicyFile(policyPath.toString());
                                view.setPolicyActionIsEdit(true);
                                view.showStatus((imported != null ? "Policy saved successfully at " : "Policy created successfully at ") + policyPath);
                            });
                        } catch (Exception ex) {
                            fxScheduler.runLater(() ->
                                    view.showError("Error saving policy: " + formatThrowable(ex)));
                        }
                    }),
                    __ -> false
            );

            dialogVm.show();
        });
    }

    /**
     * Displays observable errors emitted by the create button subscription.
     *
     * @param throwable root cause
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onCreateFilesError(Throwable throwable) {
        fxScheduler.runLater(() -> view.showError("Error triggering file creation: " + throwable.getMessage()));
    }

    /**
     * Clears selections when the reset button fires.
     *
     * @param actionEvent originating event
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onReset(ActionEvent actionEvent) {
        fxScheduler.runLater(() -> {
            model.setProjectDirectory(null);
            model.setPolicyFile(null);
            view.resetFields();
            view.setPolicyActionIsEdit(false);
        });
    }

    /**
     * Displays observable errors emitted by the reset button subscription.
     *
     * @param throwable root cause
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void onResetError(Throwable throwable) {
        fxScheduler.runLater(() -> view.showError("Error resetting fields: " + throwable.getMessage()));
    }

    /**
     * Reports errors that occur while reacting to the "create/edit policy" UI action.
     *
     * @param throwable the error emitted by the create-policy observable
     */
    private void onCreatePolicyError(Throwable throwable) {
        fxScheduler.runLater(() -> view.showError("Error opening policy dialog: " + throwable.getMessage()));
    }


    /**
     * Determines whether generation can start based on current selections.
     *
     * @return {@code true} when both project directory and policy file are filled in
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public boolean canCreateFiles() {
        return model.getProjectDirectory() != null && !model.getProjectDirectory().isEmpty() &&
                model.getPolicyFile() != null && !model.getPolicyFile().isEmpty();
    }

    /**
     * Initializes the scene graph and shows the primary window.
     *
     * @param primaryStage JavaFX primary stage
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public void start(Stage primaryStage) {
        initialize();
        Scene scene = new Scene(view.getGridPane(), 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ares UI");
        primaryStage.show();
    }

    /**
     * Releases RxJava subscriptions and terminates the executor.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public void stop() {
        disposables.dispose();
        executorService.shutdownNow();
    }

    /**
     * Formats an exception for display in the UI.
     *
     * @param throwable potential error
     * @return user-friendly message derived from the throwable
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private String formatThrowable(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args standard CLI arguments
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Attempts to parse the currently selected policy file into a dialog model for editing.
     *
     * <p>The method returns {@code null} when no policy file is selected, when the path is invalid or does not
     * exist, or when parsing fails.
     *
     * @return the parsed {@link PolicyDialogModel} to preload the dialog, or {@code null} if parsing is not possible
     */
    private PolicyDialogModel tryParseSelectedPolicyForEditing() {
        String selected = model.getPolicyFile();
        if (selected == null || selected.isBlank()) {
            return null;
        }

        Path yamlPath;
        try {
            yamlPath = Path.of(selected);
        } catch (Exception e) {
            return null;
        }

        if (!Files.exists(yamlPath)) {
            return null;
        }

        try {
            return PolicyYamlParser.parse(yamlPath);
        } catch (Exception parseEx) {
            // UI hint: allow selecting arbitrary YAMLs, but warn if not an Ares policy.
            view.showError("This doesn't look like a valid Ares security policy YAML. Please select the correct policy file.\nReason: " + parseEx.getMessage());
            return null;
        }
    }
}
