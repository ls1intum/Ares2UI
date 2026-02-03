package de.tum.cit.ase.aresUI.policy.dialog;

import de.tum.cit.ase.aresUI.policy.section.CommandRulesSection;
import de.tum.cit.ase.aresUI.policy.section.FileSystemRulesSection;
import de.tum.cit.ase.aresUI.policy.section.NetworkRulesSection;
import de.tum.cit.ase.aresUI.policy.section.PackageRulesSection;
import de.tum.cit.ase.aresUI.policy.section.SupervisedCodeSection;
import de.tum.cit.ase.aresUI.policy.section.ThreadRulesSection;
import de.tum.cit.ase.aresUI.policy.section.TimeoutSection;
import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;
import io.reactivex.rxjava3.core.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.pdfsam.rxjavafx.observables.JavaFxObservable;

import java.util.List;
import java.util.Objects;

/**
 * Policy editor dialog view.
 *
 * <p>This class is intentionally kept small and acts as a façade: it creates the stage/scene and delegates
 * the UI content and model mapping to section components and {@link PolicyDialogBinder}.
 */
public class PolicyDialogView {

    private final Stage stage;

    private final Button saveButton;
    private final Button cancelButton;
    private final Label errorLabel;

    private final SupervisedCodeSection supervisedCodeSection;
    private final FileSystemRulesSection fileSystemRulesSection;
    private final NetworkRulesSection networkRulesSection;
    private final CommandRulesSection commandRulesSection;
    private final ThreadRulesSection threadRulesSection;
    private final PackageRulesSection packageRulesSection;
    private final TimeoutSection timeoutSection;

    private final PolicyDialogBinder binder;

    /**
     * Creates a new policy dialog view.
     *
     * @param owner the owner window for the dialog
     * @throws NullPointerException if {@code owner} is {@code null}
     */
    public PolicyDialogView(Window owner) {
        Objects.requireNonNull(owner, "owner");

        this.stage = new Stage();
        this.stage.initOwner(owner);
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.setTitle("Create Security Policy");

        this.saveButton = new Button("Save");
        this.cancelButton = new Button("Cancel");

        this.errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setWrapText(true);

        // Sections
        this.supervisedCodeSection = new SupervisedCodeSection();
        this.fileSystemRulesSection = new FileSystemRulesSection(stage);
        this.networkRulesSection = new NetworkRulesSection();
        this.commandRulesSection = new CommandRulesSection();
        this.threadRulesSection = new ThreadRulesSection();
        this.packageRulesSection = new PackageRulesSection();
        this.timeoutSection = new TimeoutSection();

        this.binder = new PolicyDialogBinder(
                supervisedCodeSection,
                fileSystemRulesSection,
                networkRulesSection,
                commandRulesSection,
                threadRulesSection,
                packageRulesSection,
                timeoutSection
        );

        BorderPane outerRoot = new BorderPane();

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(10);
        form.setVgap(10);

        int row = 0;
        form.add(supervisedCodeSection.getNode(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("File system interactions:"), 0, row);
        form.add(fileSystemRulesSection.getNode(), 1, row++);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("Network connections:"), 0, row);
        form.add(networkRulesSection.getNode(), 1, row++);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("Command executions:"), 0, row);
        form.add(commandRulesSection.getNode(), 1, row++);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("Thread creations:"), 0, row);
        form.add(threadRulesSection.getNode(), 1, row++);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("Package imports:"), 0, row);
        form.add(packageRulesSection.getNode(), 1, row++);

        form.add(new javafx.scene.control.Separator(), 0, row++, 2, 1);

        form.add(new javafx.scene.control.Label("Timeouts:"), 0, row);
        form.add(timeoutSection.getNode(), 1, row++);

        form.add(errorLabel, 0, row, 2, 1);

        ScrollPane outerScroll = new ScrollPane(form);
        outerScroll.setFitToWidth(true);
        outerScroll.setPannable(true);
        outerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outerRoot.setCenter(outerScroll);

        HBox bottomButtons = new HBox(10, saveButton, cancelButton);
        bottomButtons.setPadding(new Insets(0, 12, 12, 12));
        outerRoot.setBottom(bottomButtons);

        Scene scene = new Scene(outerRoot, 1100, 900);
        stage.setMinWidth(780);
        stage.setMinHeight(620);
        stage.setScene(scene);
    }

    /**
     * Shows the dialog and blocks until it is closed.
     */
    public void show() {
        stage.showAndWait();
    }

    /**
     * Closes the dialog window.
     */
    public void close() {
        stage.close();
    }

    /**
     * Returns an observable stream of Save button action events.
     *
     * @return observable based on the Save button
     */
    public Observable<ActionEvent> saveObservable() {
        return JavaFxObservable.actionEventsOf(saveButton);
    }

    /**
     * Returns an observable stream of Cancel button action events.
     *
     * @return observable based on the Cancel button
     */
    public Observable<ActionEvent> cancelObservable() {
        return JavaFxObservable.actionEventsOf(cancelButton);
    }

    /**
     * Sets the error message text displayed in the dialog.
     *
     * @param message error message; {@code null} clears the message
     */
    public void setError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    /**
     * Clears the currently displayed error message.
     */
    public void clearError() {
        errorLabel.setText("");
    }

    /**
     * Returns the currently selected programming language configuration.
     *
     * @return selected configuration value
     */
    public String getSelectedConfig() {
        return supervisedCodeSection.getSelectedConfig();
    }

    /**
     * Returns the root package entered by the user.
     *
     * @return root package string
     */
    public String getRootPackage() {
        return supervisedCodeSection.getRootPackage();
    }

    /**
     * Returns the main class entered by the user.
     *
     * @return main class string
     */
    public String getMainClass() {
        return supervisedCodeSection.getMainClass();
    }

    /**
     * Returns the raw multi-line text of test classes.
     *
     * @return raw test class text
     */
    public String getTestClassesRaw() {
        return supervisedCodeSection.getTestClassesRaw();
    }

    /**
     * Collects file system rules from the UI.
     *
     * @return list of file system rules
     */
    public List<FileSystemRule> getFileSystemRules() {
        return fileSystemRulesSection.collect();
    }

    /**
     * Collects network connection rules from the UI.
     *
     * @return list of network connection rules
     */
    public List<NetworkConnectionRule> getNetworkConnectionRules() {
        return networkRulesSection.collect();
    }

    /**
     * Collects command execution rules from the UI.
     *
     * @return list of command execution rules
     */
    public List<CommandExecutionRule> getCommandExecutionRules() {
        return commandRulesSection.collect();
    }

    /**
     * Collects thread creation rules from the UI.
     *
     * @return list of thread creation rules
     */
    public List<ThreadCreationRule> getThreadCreationRules() {
        return threadRulesSection.collect();
    }

    /**
     * Collects package import rules from the UI.
     *
     * @return list of package import rules
     */
    public List<PackageImportRule> getPackageImportRules() {
        return packageRulesSection.collect();
    }

    /**
     * Collects timeout rules from the UI.
     *
     * @return list of timeout rules
     */
    public List<TimeoutRule> getTimeoutRules() {
        return timeoutSection.collect();
    }

    /**
     * Populates this dialog with values from an existing policy model.
     * Missing lists are treated as empty lists so legacy/partial policies can still be edited.
     */
    public void loadFromModel(PolicyDialogModel model) {
        clearError();
        binder.loadFromModel(model);
    }

    /**
     * Collects the current dialog state into a {@link PolicyDialogModel}.
     */
    public PolicyDialogModel collectToModel() {
        return binder.collectToModel();
    }
}
