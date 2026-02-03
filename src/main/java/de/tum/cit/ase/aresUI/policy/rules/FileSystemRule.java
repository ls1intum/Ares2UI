package de.tum.cit.ase.aresUI.policy.rules;

/**
 * Represents a file system rule with permissions for reading, writing, executing,
 * and deleting files within a specified directory path.
 */
public class FileSystemRule {

    private final String pathAndBelow;
    private final boolean readAllFiles;
    private final boolean overwriteAllFiles;
    private final boolean executeAllFiles;
    private final boolean deleteAllFiles;

    /**
     * Creates a file system rule.
     *
     * @param pathAndBelow base path; the rule applies to this path and all paths below
     * @param readAllFiles whether reading files is permitted
     * @param overwriteAllFiles whether overwriting files is permitted
     * @param executeAllFiles whether executing files is permitted
     * @param deleteAllFiles whether deleting files is permitted
     * @throws IllegalArgumentException if {@code pathAndBelow} is {@code null} or blank
     */
    public FileSystemRule(String pathAndBelow,
                          boolean readAllFiles,
                          boolean overwriteAllFiles,
                          boolean executeAllFiles,
                          boolean deleteAllFiles) {

        this.pathAndBelow = requireNonBlank(pathAndBelow);
        this.readAllFiles = readAllFiles;
        this.overwriteAllFiles = overwriteAllFiles;
        this.executeAllFiles = executeAllFiles;
        this.deleteAllFiles = deleteAllFiles;
    }

    /**
     * Ensures the given string is non-blank and returns its trimmed value.
     *
     * @param s input string
     * @return trimmed string
     * @throws IllegalArgumentException if the string is {@code null} or blank
     */
    private static String requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("pathAndBelow must not be blank");
        }
        return s.trim();
    }

    /**
     * Returns the base path for this rule.
     *
     * @return path value
     */
    public String getPathAndBelow() {
        return pathAndBelow;
    }

    /**
     * Indicates whether reading files is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isReadAllFiles() {
        return readAllFiles;
    }

    /**
     * Indicates whether overwriting files is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isOverwriteAllFiles() {
        return overwriteAllFiles;
    }

    /**
     * Indicates whether executing files is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isExecuteAllFiles() {
        return executeAllFiles;
    }

    /**
     * Indicates whether deleting files is permitted.
     *
     * @return {@code true} if permitted
     */
    public boolean isDeleteAllFiles() {
        return deleteAllFiles;
    }
}
