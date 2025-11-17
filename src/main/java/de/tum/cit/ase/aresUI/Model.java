package de.tum.cit.ase.aresUI;

/**
 * Mutable data holder for user selections.
 *
 * <p>Description: Keeps track of the selected project directory and policy file so the ViewModel can
 * validate user input and trigger generation safely.
 *
 * <p>Design Rationale: Centralizing state management simplifies binding between the UI and business logic.
 *
 * @since 0.0.1
 * @author Markus Paulsen
 * @version 0.0.1
 */
public class Model {
    /**
     * Absolute path of the selected project directory.
     */
    private String projectDirectory;
    /**
     * Absolute path of the selected policy file.
     */
    private String policyFile;

    /**
     * Returns the project directory path.
     *
     * @return the directory path or {@code null} when not set
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public String getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Updates the project directory path.
     *
     * @param projectDirectory the directory to remember
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    /**
     * Returns the policy file path.
     *
     * @return the file path or {@code null} when not set
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public String getPolicyFile() {
        return policyFile;
    }

    /**
     * Updates the policy file path.
     *
     * @param policyFile the policy file to remember
     * @since 0.0.1
     * @author Markus Paulsen
     */
    public void setPolicyFile(String policyFile) {
        this.policyFile = policyFile;
    }
}
