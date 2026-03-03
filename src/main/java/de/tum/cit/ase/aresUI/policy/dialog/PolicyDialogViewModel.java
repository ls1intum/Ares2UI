package de.tum.cit.ase.aresUI.policy.dialog;

import de.tum.cit.ase.aresUI.SelectionProvider;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.event.ActionEvent;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Coordinates the policy dialog UI workflow by translating view interactions into a validated {@link PolicyDialogModel}
 * and triggering persistence via a save callback.
 */
public class PolicyDialogViewModel {

    private final PolicyDialogViewContract view;
    private final SelectionProvider selectionProvider;
    private final Window owner;
    private final BiConsumer<PolicyDialogModel, Path> onSave;
    private final Predicate<Path> isUnsafeLocation;

    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Creates a view-model for the policy dialog.
     *
     * @param view dialog view contract
     * @param selectionProvider provider used to select the output file
     * @param owner owner window for file chooser dialogs; may be {@code null}
     * @param onSave callback invoked after successful validation and file selection
     * @param isUnsafeLocation predicate used to reject unsafe output locations
     * @throws NullPointerException if {@code view}, {@code selectionProvider}, {@code onSave}, or {@code isUnsafeLocation} is {@code null}
     */
    public PolicyDialogViewModel(PolicyDialogViewContract view,
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
            if (model == null) {
                return; // validation failed; error already shown
            }

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
     * Collects the current view state into a {@link PolicyDialogModel} and validates it.
     *
     * <p>The model is collected exactly once. Constructor-level validation errors from the model
     * (e.g. blank fields) are translated into a generic user-friendly message. Post-construction checks
     * (e.g. at-most-one timeout) are performed by {@link PolicyModelValidator}.
     *
     * @return the validated policy model, or {@code null} if validation failed (error already shown)
     */
    private PolicyDialogModel buildModelFromView() {
        // Collect the model once; the constructor may throw for blank/missing fields
        PolicyDialogModel model;
        try {
            model = view.collectToModel();
        } catch (IllegalArgumentException ex) {
            view.setError("Invalid policy configuration. Please review your inputs.");
            return null;
        }

        // Run additional UI-level validation on the constructed model
        List<String> errors = PolicyModelValidator.validate(model);
        if (!errors.isEmpty()) {
            view.setError(errors.get(0));
            return null;
        }

        return model;
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
}
