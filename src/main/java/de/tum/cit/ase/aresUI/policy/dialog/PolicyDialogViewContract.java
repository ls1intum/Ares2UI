package de.tum.cit.ase.aresUI.policy.dialog;

import io.reactivex.rxjava3.core.Observable;
import javafx.event.ActionEvent;

/**
 * Contract for the policy dialog view, defining the interaction surface used by {@link PolicyDialogViewModel}.
 *
 * <p>Extracting this interface decouples the ViewModel from the concrete JavaFX implementation, allowing
 * unit tests to supply a lightweight mock without booting the JavaFX toolkit.
 */
public interface PolicyDialogViewContract {

    /**
     * Returns an observable stream of Save button action events.
     *
     * @return observable based on the Save button
     */
    Observable<ActionEvent> saveObservable();

    /**
     * Returns an observable stream of Cancel button action events.
     *
     * @return observable based on the Cancel button
     */
    Observable<ActionEvent> cancelObservable();

    /**
     * Sets the error message text displayed in the dialog.
     *
     * @param message error message; {@code null} clears the message
     */
    void setError(String message);

    /**
     * Clears the currently displayed error message.
     */
    void clearError();

    /**
     * Closes the dialog window.
     */
    void close();

    /**
     * Shows the dialog and blocks until it is closed.
     */
    void show();

    /**
     * Populates the dialog with values from an existing policy model.
     *
     * @param model the model to load
     */
    void loadFromModel(PolicyDialogModel model);

    /**
     * Collects the current dialog state into a {@link PolicyDialogModel}.
     *
     * @return the collected model
     */
    PolicyDialogModel collectToModel();
}
