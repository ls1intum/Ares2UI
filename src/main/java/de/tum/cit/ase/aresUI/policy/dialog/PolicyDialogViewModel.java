package de.tum.cit.ase.aresUI.policy.dialog;

import de.tum.cit.ase.aresUI.SelectionProvider;
import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.event.ActionEvent;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Coordinates the policy dialog UI workflow by translating view interactions into a validated {@link PolicyDialogModel}
 * and triggering persistence via a save callback.
 */
public class PolicyDialogViewModel {

    private final PolicyDialogView view;
    private final SelectionProvider selectionProvider;
    private final Window owner;
    private final BiConsumer<PolicyDialogModel, Path> onSave;
    private final Predicate<Path> isUnsafeLocation;

    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Creates a view-model for the policy dialog.
     *
     * @param view dialog view facade
     * @param selectionProvider provider used to select the output file
     * @param owner owner window for file chooser dialogs; may be {@code null}
     * @param onSave callback invoked after successful validation and file selection
     * @param isUnsafeLocation predicate used to reject unsafe output locations
     * @throws NullPointerException if {@code view}, {@code selectionProvider}, {@code onSave}, or {@code isUnsafeLocation} is {@code null}
     */
    public PolicyDialogViewModel(PolicyDialogView view,
                                 SelectionProvider selectionProvider,
                                 Window owner,
                                 BiConsumer<PolicyDialogModel, Path> onSave,
                                 Predicate<Path> isUnsafeLocation) {
        this.view = Objects.requireNonNull(view, "view");
        this.selectionProvider = Objects.requireNonNull(selectionProvider, "selectionProvider");
        this.owner = owner;
        this.onSave = Objects.requireNonNull(onSave, "onSave");
        this.isUnsafeLocation = Objects.requireNonNull(isUnsafeLocation, "isUnsafeLocation");
    }

    /**
     * Subscribes to view events (save/cancel) and binds handlers.
     */
    public void initialize() {
        disposables.add(view.saveObservable().subscribe(this::handleSave, this::handleError));
        disposables.add(view.cancelObservable().subscribe(e -> handleCancel(), this::handleError));
    }

    /**
     * Shows the dialog and disposes all subscriptions once the dialog returns.
     */
    public void show() {
        initialize();
        view.show();
        disposables.dispose();
    }

    /**
     * Handles the Save button action.
     *
     * <p>This method validates the current view state, prompts the user for an output path, applies the
     * unsafe-location check, and then invokes {@code onSave}.
     *
     * @param e the JavaFX action event
     */
    private void handleSave(ActionEvent e) {
        view.clearError();

        try {
            PolicyDialogModel model = buildModelFromView();

            Optional<java.io.File> optionalFile = selectionProvider.selectSavePolicyFile(owner, "security-policy.yaml");
            if (optionalFile.isEmpty()) {
                return;
            }

            Path outputPath = optionalFile.get().toPath();

            if (isUnsafeLocation.test(outputPath)) {
                view.setError("Refusing to save inside the selected project folder (risk of exposing policy to students). Please choose another location.");
                return;
            }

            onSave.accept(model, outputPath);
            view.close();
        } catch (Exception ex) {
            view.setError(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        }
    }

    /**
     * Builds a {@link PolicyDialogModel} from the current view state.
     *
     * @return the constructed policy model
     * @throws IllegalArgumentException if any required field is missing or any rule list contains invalid data
     */
    private PolicyDialogModel buildModelFromView() {
        String config = trimOrNull(view.getSelectedConfig());
        String rootPackage = trimOrNull(view.getRootPackage());
        String mainClass = trimOrNull(view.getMainClass());

        List<FileSystemRule> fsRules = view.getFileSystemRules();
        validateFileSystemRules(fsRules);

        List<NetworkConnectionRule> networkRules = view.getNetworkConnectionRules();
        validateNetworkRules(networkRules);

        List<CommandExecutionRule> commandRules = view.getCommandExecutionRules();
        validateCommandRules(commandRules);

        List<ThreadCreationRule> threadRules = view.getThreadCreationRules();
        validateThreadRules(threadRules);

        List<PackageImportRule> packageRules = view.getPackageImportRules();
        validatePackageRules(packageRules);

        List<TimeoutRule> timeoutRules = view.getTimeoutRules();
        validateTimeoutRules(timeoutRules);

        List<String> testClasses = parseTestClasses(view.getTestClassesRaw());

        if (config == null) throw new IllegalArgumentException("Please select a programming language configuration.");
        if (rootPackage == null) throw new IllegalArgumentException("Root package must not be empty.");
        if (mainClass == null) throw new IllegalArgumentException("Main class must not be empty.");
        if (testClasses.isEmpty()) throw new IllegalArgumentException("Please provide at least one test class.");

        return new PolicyDialogModel(
                config,
                rootPackage,
                mainClass,
                testClasses,
                fsRules,
                networkRules,
                commandRules,
                threadRules,
                packageRules,
                timeoutRules
        );
    }

    /**
     * Parses a multi-line text field into a list of non-empty class names.
     *
     * @param raw raw text (may be {@code null})
     * @return list of trimmed, non-empty lines
     */
    private List<String> parseTestClasses(String raw) {
        if (raw == null) return List.of();
        return Arrays.stream(raw.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Trims the given string and converts blank values to {@code null}.
     *
     * @param s input value
     * @return trimmed value or {@code null} when blank
     */
    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Handles the Cancel button action by closing the dialog.
     */
    private void handleCancel() {
        view.close();
    }

    /**
     * Handles errors coming from reactive subscriptions by presenting them to the user.
     *
     * @param t the error raised by an observable
     */
    private void handleError(Throwable t) {
        view.setError("Unexpected error: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName()));
    }

    /**
     * Validates file system rules and rejects empty paths.
     *
     * @param rules file system rule list
     * @throws IllegalArgumentException if an entry has an empty path
     */
    private void validateFileSystemRules(List<FileSystemRule> rules) {
        if (rules.isEmpty()) return;

        boolean hasEmptyRule = rules.stream()
                .anyMatch(r -> r.getPathAndBelow() == null || r.getPathAndBelow().trim().isEmpty());
        if (hasEmptyRule) {
            throw new IllegalArgumentException("Please fill the file system path or delete the empty rule.");
        }
    }

    /**
     * Validates network rules by requiring a host and a valid port.
     *
     * @param rules network rule list
     * @throws IllegalArgumentException if an entry has an empty host or an invalid port
     */
    private void validateNetworkRules(List<NetworkConnectionRule> rules) {
        boolean hasEmptyHost = rules.stream().anyMatch(r -> r.getHost() == null || r.getHost().trim().isEmpty());
        if (hasEmptyHost) {
            throw new IllegalArgumentException("Please fill the network host or delete the empty rule.");
        }
        boolean invalidPort = rules.stream().anyMatch(r -> r.getPort() < 1 || r.getPort() > 65535);
        if (invalidPort) {
            throw new IllegalArgumentException("Network port must be between 1 and 65535.");
        }
    }

    /**
     * Validates command rules by requiring a non-empty command.
     *
     * @param rules command rule list
     * @throws IllegalArgumentException if an entry has an empty command
     */
    private void validateCommandRules(List<CommandExecutionRule> rules) {
        boolean hasEmptyCommand = rules.stream().anyMatch(r -> r.getCommand() == null || r.getCommand().trim().isEmpty());
        if (hasEmptyCommand) {
            throw new IllegalArgumentException("Please fill the command to execute or delete the empty rule.");
        }
    }

    /**
     * Validates thread rules by requiring a non-negative thread count and a class name.
     *
     * @param rules thread rule list
     * @throws IllegalArgumentException if an entry is invalid
     */
    private void validateThreadRules(List<ThreadCreationRule> rules) {
        boolean invalid = rules.stream().anyMatch(r -> r.getNumberOfThreads() < 0 || r.getThreadClass() == null || r.getThreadClass().trim().isEmpty());
        if (invalid) {
            throw new IllegalArgumentException("Thread rules need a non-negative thread count and a class name.");
        }
    }

    /**
     * Validates package rules by requiring a non-empty package name.
     *
     * @param rules package rule list
     * @throws IllegalArgumentException if an entry has an empty package name
     */
    private void validatePackageRules(List<PackageImportRule> rules) {
        boolean invalid = rules.stream().anyMatch(r -> r.getPackageName() == null || r.getPackageName().trim().isEmpty());
        if (invalid) {
            throw new IllegalArgumentException("Please fill the package name or delete the empty rule.");
        }
    }

    /**
     * Validates timeout rules.
     *
     * <p>Rules must contain only positive values and at most one entry.
     *
     * @param rules timeout rule list
     * @throws IllegalArgumentException if an entry is non-positive or more than one entry exists
     */
    private void validateTimeoutRules(List<TimeoutRule> rules) {
        boolean invalid = rules.stream().anyMatch(r -> r.getTimeoutSeconds() <= 0);
        if (invalid) {
            throw new IllegalArgumentException("Timeout must be a positive number of seconds.");
        }
        if (rules.size() > 1) {
            throw new IllegalArgumentException("Please provide only one timeout entry (or remove all of them).");
        }
    }
}
