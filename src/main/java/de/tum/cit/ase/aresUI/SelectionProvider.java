package de.tum.cit.ase.aresUI;

import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

/**
 * Strategy interface for selecting directories and files via the UI.
 *
 * <p>Description: The selection provider abstracts away JavaFX chooser dialogs so that views can interact
 * with custom selection logic during tests or when integrating other UI toolkits.
 *
 * <p>Design Rationale: Encapsulating selection logic keeps the ViewModel decoupled from platform specifics
 * and improves testability.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public interface SelectionProvider {

    /**
     * Requests the user to choose the Maven project directory.
     *
     * @param owner the owner window hosting the chooser
     * @return an {@link Optional} containing the selected directory or {@link Optional#empty()} if cancelled
     * @since 0.0.1
     * @author Markus Paulsen
     */
    Optional<File> selectProjectDirectory(Window owner);

    /**
     * Requests the user to choose the YAML policy file to feed into the generator.
     *
     * @param owner the owner window hosting the chooser
     * @return an {@link Optional} containing the selected file or {@link Optional#empty()} if cancelled
     * @since 0.0.1
     * @author Markus Paulsen
     */
    Optional<File> selectPolicyFile(Window owner);
}

