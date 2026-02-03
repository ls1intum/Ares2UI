package de.tum.cit.ase.aresUI;

import io.reactivex.rxjava3.core.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.pdfsam.rxjavafx.observables.JavaFxObservable;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * JavaFX view exposing observable user interaction streams.
 *
 * <p>Description: Builds the UI controls, connects them to RxJava streams, and offers helper methods for
 * updating the display from the ViewModel.
 *
 * <p>Design Rationale: Keeping the view dumb and reactive simplifies the ViewModel and supports clean MVVM
 * boundaries.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class View {
    /**
     * Root layout hosting all UI controls.
     */
    private final GridPane gridPane;
    /**
     * Text field displaying the chosen project directory.
     */
    private final TextField projectTextField;
    /**
     * Button that opens the directory chooser.
     */
    private final Button projectButton;
    /**
     * Text field displaying the selected policy file.
     */
    private final TextField policyTextField;
    /**
     * Button used to select the policy file.
     */
    private final Button policyButton;
    /**
     * Button that triggers generation.
     */
    private final Button createFilesButton;
    /**
     * Button that resets the form.
     */
    private final Button resetButton;
    /**
     * Button that triggers policy creation.
     */
    private final Button createPolicyButton;
    /**
     * Multi-line text area used for status and error reporting.
     */
    private final TextArea statusTextArea;
    /**
     * Strategy for selecting directories and files.
     */
    private final SelectionProvider selectionProvider;

    private static final String CREATE_POLICY_LABEL = "Create Policy";
    private static final String EDIT_POLICY_LABEL = "Edit Policy";

    /**
     * Creates the view with the production {@link DefaultSelectionProvider}.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public View() {
        this(new DefaultSelectionProvider());
    }

    /**
     * Creates the view with a custom {@link SelectionProvider}, primarily useful for tests.
     *
     * @param selectionProvider provider responsible for opening chooser dialogs
     * @since 0.0.1
     * @author Markus Paulsen
     */
    View(SelectionProvider selectionProvider) {
        this.selectionProvider = Objects.requireNonNull(selectionProvider, "selectionProvider");
        this.gridPane = new GridPane();
        this.projectTextField = new TextField();
        this.projectButton = new Button("Choose Folder");
        this.policyTextField = new TextField();
        this.policyButton = new Button("Choose File");
        this.createPolicyButton = new Button(CREATE_POLICY_LABEL);
        this.statusTextArea = new TextArea();
        this.createFilesButton = new Button("Create Files");
        this.resetButton = new Button("Reset");
        initializeUI();
    }

    /**
     * Provides access to the root grid pane for embedding into scenes.
     *
     * @return the main grid pane
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    /**
     * Exposes the project text field for direct manipulation.
     *
     * @return the project text field
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public TextField getProjectTextField() {
        return projectTextField;
    }

    /**
     * Exposes the policy text field for direct manipulation.
     *
     * @return the policy text field
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public TextField getPolicyTextField() {
        return policyTextField;
    }

    /**
     * Provides access to the directory chooser button.
     *
     * @return the project button
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Button getProjectButton() {
        return projectButton;
    }

    /**
     * Provides access to the policy chooser button.
     *
     * @return the policy button
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Button getPolicyButton() {
        return policyButton;
    }

    /**
     * Provides access to the create button.
     *
     * @return the create button
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Button getCreateFilesButton() {
        return createFilesButton;
    }

    /**
     * Provides access to the reset button.
     *
     * @return the reset button
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Button getResetButton() {
        return resetButton;
    }

    /**
     * Provides access to the status text area.
     *
     * @return the status text area
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public TextArea getStatusTextArea() {
        return statusTextArea;
    }

    /**
     * Updates the project directory field text.
     *
     * @param directory directory value to display
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void updateProjectDirectory(String directory) {
        projectTextField.setText(directory);
    }

    /**
     * Updates the policy file field text.
     *
     * @param file file path to display
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void updatePolicyFile(String file) {
        policyTextField.setText(file);
    }

    /**
     * Configures layout constraints and control defaults.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void initializeUI() {
        // Add consistent outer padding + gaps so controls don't touch the window edges.
        gridPane.setPadding(new Insets(12));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label projectLabel = new Label("Project Folder:");
        projectTextField.setEditable(false);
        gridPane.add(projectLabel, 0, 0);
        gridPane.add(projectTextField, 1, 0);
        gridPane.add(projectButton, 2, 0);

        Label policyLabel = new Label("YAML Policy File:");
        policyTextField.setEditable(false);
        gridPane.add(policyLabel, 0, 2);
        gridPane.add(policyTextField, 1, 2);

        HBox policyButtons = new HBox(8, policyButton, createPolicyButton);
        policyButtons.setAlignment(Pos.CENTER_LEFT);
        gridPane.add(policyButtons, 2, 2);

        gridPane.add(createFilesButton, 1, 3);
        gridPane.add(resetButton, 2, 3);

        Label statusLabel = new Label("Status:");
        gridPane.add(statusLabel, 0, 4);
        statusTextArea.setEditable(false);
        statusTextArea.setWrapText(true);
        statusTextArea.setPrefRowCount(5);
        gridPane.add(statusTextArea, 1, 4, 2, 4);

        // Add a bit of breathing room around the status field.
        GridPane.setMargin(statusTextArea, new Insets(6, 0, 0, 0));

        GridPane.setHgrow(projectTextField, Priority.ALWAYS);
        GridPane.setHgrow(policyTextField, Priority.ALWAYS);
        GridPane.setHgrow(statusTextArea, Priority.ALWAYS);
        GridPane.setVgrow(statusTextArea, Priority.ALWAYS);
        GridPane.setHgrow(createFilesButton, Priority.ALWAYS);
        GridPane.setHgrow(resetButton, Priority.ALWAYS);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.NEVER);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);

        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.NEVER);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();
        row5.setVgrow(Priority.ALWAYS);

        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5);
    }

    /**
     * Creates an observable emitting selected directories.
     *
     * @return observable of directory selections
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Observable<File> projectDirectoryObservable() {
        return JavaFxObservable.actionEventsOf(projectButton)
                .map(__ -> selectionProvider.selectProjectDirectory(getWindow()))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Creates an observable emitting selected policy files.
     *
     * @return observable of policy file selections
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Observable<File> policyFileObservable() {
        return JavaFxObservable.actionEventsOf(policyButton)
                .map(__ -> selectionProvider.selectPolicyFile(getWindow()))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Exposes create button clicks as an observable.
     *
     * @return observable of create button events
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Observable<ActionEvent> createFilesObservable() {
        return JavaFxObservable.actionEventsOf(createFilesButton);
    }

    public Observable<ActionEvent> createPolicyObservable() {
        return JavaFxObservable.actionEventsOf(createPolicyButton);
    }

    /**
     * Exposes reset button clicks as an observable.
     *
     * @return observable of reset button events
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Observable<ActionEvent> resetObservable() {
        return JavaFxObservable.actionEventsOf(resetButton);
    }

    /**
     * Returns the hosting window, if available.
     *
     * @return owner window or {@code null} if detached
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public Window getWindow() {
        Scene scene = gridPane.getScene();
        return scene != null ? scene.getWindow() : null;
    }

    /**
     * Appends a status message to the log.
     *
     * @param message message to append
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void showStatus(String message) {
        appendMessage("STATUS", message);
    }

    /**
     * Appends an error message to the log.
     *
     * @param message message to append
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void showError(String message) {
        appendMessage("ERROR", message);
    }

    /**
     * Clears both text fields and the status area.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void resetFields() {
        projectTextField.clear();
        policyTextField.clear();
        clearStatus();
    }

    /**
     * Clears the status area only.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void clearStatus() {
        statusTextArea.clear();
    }

    /**
     * Appends a formatted entry to the status area.
     *
     * @param prefix prefix describing the severity
     * @param message message to append
     * @since 0.0.1
     * @author Markus Paulsen
     */
    private void appendMessage(String prefix, String message) {
        statusTextArea.appendText(String.format("[%s] %s%n", prefix, message));
    }

    /**
     * Switches the policy action button label.
     *
     * <p>If a policy file is selected, the main action becomes "Edit Policy".
     */
    public void setPolicyActionIsEdit(boolean isEdit) {
        createPolicyButton.setText(isEdit ? EDIT_POLICY_LABEL : CREATE_POLICY_LABEL);
    }
}
