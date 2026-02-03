package de.tum.cit.ase.aresUI;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Default selection provider that uses JavaFX chooser dialogs.
 *
 * <p>Description: The provider wraps {@link DirectoryChooser} and {@link FileChooser}, but exposes them
 * via injectable functions so tests can supply deterministic behavior.
 *
 * <p>Design Rationale: Introducing constructor injection for the chooser functions reduces the need for
 * heavy construction mocking and promotes composability.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class DefaultSelectionProvider implements SelectionProvider {

    /**
     * Function that opens the directory chooser and returns the selection.
     */
    private final Function<Window, File> directoryChooser;
    /**
     * Function that opens the file chooser and returns the selection.
     */
    private final Function<Window, File> policyChooser;

    /**
     * Builds the provider with production JavaFX chooser logic.
     *
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public DefaultSelectionProvider() {
        this(window -> {
            DirectoryChooser chooser = new DirectoryChooser();
            return chooser.showDialog(window);
        }, window -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"));
            return chooser.showOpenDialog(window);
        });
    }

    /**
     * Creates a provider with custom chooser functions (primarily for testing).
     *
     * @param directoryChooser function returning the selected directory
     * @param policyChooser function returning the selected policy file
     * @since 0.0.1
     * @author Markus Paulsen
     */
    DefaultSelectionProvider(Function<Window, File> directoryChooser, Function<Window, File> policyChooser) {
        this.directoryChooser = Objects.requireNonNull(directoryChooser, "directoryChooser");
        this.policyChooser = Objects.requireNonNull(policyChooser, "policyChooser");
    }

    /**
     * Opens the directory chooser and returns the outcome.
     *
     * @param owner window used as the chooser parent
     * @return an {@link Optional} describing the selected directory
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public Optional<File> selectProjectDirectory(Window owner) {
        return Optional.ofNullable(directoryChooser.apply(owner));
    }

    /**
     * Opens the file chooser and returns the outcome.
     *
     * @param owner window used as the chooser parent
     * @return an {@link Optional} describing the selected file
     * @since 0.0.1
     * @author Markus Paulsen
     */
    @Override
    public Optional<File> selectPolicyFile(Window owner) {
        return Optional.ofNullable(policyChooser.apply(owner));
    }

    /**
     * Opens a save-file dialog so the user can choose where to store a security policy YAML file.
     *
     * <p>The dialog is configured with a title and a suggested initial file name, and it restricts the
     * selectable file types to YAML extensions.
     *
     * @param owner the owner window used as the parent for the chooser dialog
     * @param suggestedFileName the initially suggested file name shown in the dialog
     * @return an {@link Optional} describing the selected file, or {@link Optional#empty()} if the user cancels
     * @throws NullPointerException if {@code owner} is {@code null}
     */
    @Override
    public Optional<File> selectSavePolicyFile(Window owner, String suggestedFileName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Security Policy");
        chooser.setInitialFileName(suggestedFileName);

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("YAML files", "*.yaml", "*.yml")
        );

        File file = chooser.showSaveDialog(owner);
        return Optional.ofNullable(file);
    }
}
