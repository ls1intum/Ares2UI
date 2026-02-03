package de.tum.cit.ase.aresUI.policy.dialog;

import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable, validated state of the policy dialog used as the canonical input for serialization and persistence.
 */
public class PolicyDialogModel {

    private final String programmingLanguageConfiguration;
    private final String rootPackage;
    private final String mainClass;
    private final List<String> testClasses;

    private final List<FileSystemRule> fileSystemRules;
    private final List<NetworkConnectionRule> networkConnectionRules;
    private final List<CommandExecutionRule> commandExecutionRules;
    private final List<ThreadCreationRule> threadCreationRules;
    private final List<PackageImportRule> packageImportRules;
    private final List<TimeoutRule> timeoutRules;

    /**
     * Creates an immutable policy model.
     *
     * <p>All string fields are trimmed and validated to be non-blank. The {@code testClasses} list must not
     * be empty. Rule lists are copied, null elements are removed, and rule string values are trimmed.
     *
     * @param programmingLanguageConfiguration programming language configuration identifier
     * @param rootPackage root package of the supervised code
     * @param mainClass main class name inside the root package
     * @param testClasses list of test class names (must not be empty)
     * @param fileSystemRules file system access rules
     * @param networkConnectionRules network connection rules
     * @param commandExecutionRules command execution rules
     * @param threadCreationRules thread creation rules
     * @param packageImportRules package import rules
     * @param timeoutRules timeout rules
     * @throws NullPointerException if any list parameter is {@code null}
     * @throws IllegalArgumentException if required strings are blank or {@code testClasses} is empty
     */
    public PolicyDialogModel(String programmingLanguageConfiguration,
                             String rootPackage,
                             String mainClass,
                             List<String> testClasses,
                             List<FileSystemRule> fileSystemRules,
                             List<NetworkConnectionRule> networkConnectionRules,
                             List<CommandExecutionRule> commandExecutionRules,
                             List<ThreadCreationRule> threadCreationRules,
                             List<PackageImportRule> packageImportRules,
                             List<TimeoutRule> timeoutRules) {

        this.programmingLanguageConfiguration = requireNonBlank(programmingLanguageConfiguration, "programmingLanguageConfiguration");
        this.rootPackage = requireNonBlank(rootPackage, "rootPackage");
        this.mainClass = requireNonBlank(mainClass, "mainClass");

        this.testClasses = List.copyOf(Objects.requireNonNull(testClasses, "testClasses"));
        if (this.testClasses.isEmpty()) {
            throw new IllegalArgumentException("testClasses must not be empty");
        }

        this.fileSystemRules = List.copyOf(cleanFileSystemRules(fileSystemRules));
        this.networkConnectionRules = List.copyOf(cleanNetworkConnectionRules(networkConnectionRules));
        this.commandExecutionRules = List.copyOf(cleanCommandExecutionRules(commandExecutionRules));
        this.threadCreationRules = List.copyOf(cleanThreadCreationRules(threadCreationRules));
        this.packageImportRules = List.copyOf(cleanPackageImportRules(packageImportRules));
        this.timeoutRules = List.copyOf(cleanTimeoutRules(timeoutRules));
    }

    /**
     * Backwards-compatible constructor that only configures supervised-code metadata and file system rules.
     *
     * <p>All other rule lists are initialized as empty lists.
     *
     * @param programmingLanguageConfiguration programming language configuration identifier
     * @param rootPackage root package of the supervised code
     * @param mainClass main class name inside the root package
     * @param testClasses list of test class names (must not be empty)
     * @param fileSystemRules file system access rules
     */
    public PolicyDialogModel(String programmingLanguageConfiguration,
                             String rootPackage,
                             String mainClass,
                             List<String> testClasses,
                             List<FileSystemRule> fileSystemRules) {
        this(programmingLanguageConfiguration,
                rootPackage,
                mainClass,
                testClasses,
                fileSystemRules,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    /**
     * Validates and trims the given string.
     *
     * @param s the value to validate
     * @param name parameter name used for error messages
     * @return the trimmed string
     * @throws IllegalArgumentException if the string is {@code null} or blank
     */
    private static String requireNonBlank(String s, String name) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return s.trim();
    }

    /**
     * Normalizes file system rules by removing null entries and re-creating rules with trimmed paths.
     *
     * @param fileSystemRules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code fileSystemRules} is {@code null}
     */
    private static List<FileSystemRule> cleanFileSystemRules(List<FileSystemRule> fileSystemRules) {
        return Objects.requireNonNull(fileSystemRules, "fileSystemRules").stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getPathAndBelow() != null && !r.getPathAndBelow().trim().isEmpty())
                .map(r -> new FileSystemRule(
                        r.getPathAndBelow().trim(),
                        r.isReadAllFiles(),
                        r.isOverwriteAllFiles(),
                        r.isExecuteAllFiles(),
                        r.isDeleteAllFiles()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Normalizes network connection rules by removing null entries and trimming host names.
     *
     * @param rules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code rules} is {@code null}
     * @throws IllegalArgumentException if a rule contains an invalid port (propagated from constructor)
     */
    private static List<NetworkConnectionRule> cleanNetworkConnectionRules(List<NetworkConnectionRule> rules) {
        return Objects.requireNonNull(rules, "networkConnectionRules").stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getHost() != null && !r.getHost().trim().isEmpty())
                .map(r -> new NetworkConnectionRule(
                        r.getHost().trim(),
                        r.getPort(),
                        r.isOpenConnections(),
                        r.isSendData(),
                        r.isReceiveData()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Normalizes command execution rules by removing null entries, trimming the command, and trimming
     * argument strings while dropping empty ones.
     *
     * @param rules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code rules} is {@code null}
     */
    private static List<CommandExecutionRule> cleanCommandExecutionRules(List<CommandExecutionRule> rules) {
        return Objects.requireNonNull(rules, "commandExecutionRules").stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getCommand() != null && !r.getCommand().trim().isEmpty())
                .map(r -> new CommandExecutionRule(
                        r.getCommand().trim(),
                        r.getArguments().stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Normalizes thread creation rules by removing null entries and trimming the thread class name.
     *
     * @param rules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code rules} is {@code null}
     */
    private static List<ThreadCreationRule> cleanThreadCreationRules(List<ThreadCreationRule> rules) {
        return Objects.requireNonNull(rules, "threadCreationRules").stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getThreadClass() != null && !r.getThreadClass().trim().isEmpty())
                .map(r -> new ThreadCreationRule(r.getNumberOfThreads(), r.getThreadClass().trim()))
                .collect(Collectors.toList());
    }

    /**
     * Normalizes package import rules by removing null entries and trimming the package name.
     *
     * @param rules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code rules} is {@code null}
     */
    private static List<PackageImportRule> cleanPackageImportRules(List<PackageImportRule> rules) {
        return Objects.requireNonNull(rules, "packageImportRules").stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getPackageName() != null && !r.getPackageName().trim().isEmpty())
                .map(r -> new PackageImportRule(r.getPackageName().trim()))
                .collect(Collectors.toList());
    }

    /**
     * Normalizes timeout rules by removing null entries and re-creating rules.
     *
     * @param rules input list
     * @return cleaned list (never {@code null})
     * @throws NullPointerException if {@code rules} is {@code null}
     * @throws IllegalArgumentException if a timeout entry is non-positive (propagated from constructor)
     */
    private static List<TimeoutRule> cleanTimeoutRules(List<TimeoutRule> rules) {
        return Objects.requireNonNull(rules, "timeoutRules").stream()
                .filter(Objects::nonNull)
                .map(r -> new TimeoutRule(r.getTimeoutSeconds()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the programming language configuration identifier.
     *
     * @return configuration identifier
     */
    public String getProgrammingLanguageConfiguration() {
        return programmingLanguageConfiguration;
    }

    /**
     * Returns the root package of the supervised code.
     *
     * @return root package
     */
    public String getRootPackage() {
        return rootPackage;
    }

    /**
     * Returns the main class name.
     *
     * @return main class
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Returns the list of test class names.
     *
     * @return immutable list of test classes
     */
    public List<String> getTestClasses() {
        return testClasses;
    }

    /**
     * Returns configured file system rules.
     *
     * @return immutable list of file system rules
     */
    public List<FileSystemRule> getFileSystemRules() {
        return fileSystemRules;
    }

    /**
     * Returns configured network connection rules.
     *
     * @return immutable list of network connection rules
     */
    public List<NetworkConnectionRule> getNetworkConnectionRules() {
        return networkConnectionRules;
    }

    /**
     * Returns configured command execution rules.
     *
     * @return immutable list of command execution rules
     */
    public List<CommandExecutionRule> getCommandExecutionRules() {
        return commandExecutionRules;
    }

    /**
     * Returns configured thread creation rules.
     *
     * @return immutable list of thread creation rules
     */
    public List<ThreadCreationRule> getThreadCreationRules() {
        return threadCreationRules;
    }

    /**
     * Returns configured package import rules.
     *
     * @return immutable list of package import rules
     */
    public List<PackageImportRule> getPackageImportRules() {
        return packageImportRules;
    }

    /**
     * Returns configured timeout rules.
     *
     * @return immutable list of timeout rules
     */
    public List<TimeoutRule> getTimeoutRules() {
        return timeoutRules;
    }
}