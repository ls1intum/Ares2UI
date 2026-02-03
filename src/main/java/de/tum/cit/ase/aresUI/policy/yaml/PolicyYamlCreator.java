package de.tum.cit.ase.aresUI.policy.yaml;

import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders a validated {@link PolicyDialogModel} into its YAML representation for persistence or export.
 */
public class PolicyYamlCreator {

    /**
     * Creates a policy YAML file at the given output path.
     *
     * <p>The output path is converted to an absolute path and parent directories are created if necessary.
     * The YAML content is derived from the provided {@link PolicyDialogModel}.
     *
     * @param outputPath target file path
     * @param model policy model to serialize
     * @return the absolute path that was written
     * @throws NullPointerException if {@code outputPath} or {@code model} is {@code null}
     * @throws IOException if writing the file fails
     */
    public Path createPolicyYamlAt(Path outputPath, PolicyDialogModel model) throws IOException {
        Path abs = outputPath.toAbsolutePath();
        Path parent = abs.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String testClassesYaml = model.getTestClasses().stream()
                .map(tc -> "    - \"" + escape(tc) + "\"")
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "\n") + b);

        String fsSection = renderFileSystemRules(model);
        String networkSection = renderNetworkRules(model);
        String commandSection = renderCommandRules(model);
        String threadSection = renderThreadRules(model);
        String packageSection = renderPackageRules(model);
        String timeoutSection = renderTimeoutRules(model);

        String yaml = "regardingTheSupervisedCode:\n"
                + "  theFollowingProgrammingLanguageConfigurationIsUsed: " + escapePlain(model.getProgrammingLanguageConfiguration()) + "\n"
                + "  theSupervisedCodeUsesTheFollowingPackage: \"" + escape(model.getRootPackage()) + "\"\n"
                + "  theMainClassInsideThisPackageIs: \"" + escape(model.getMainClass()) + "\"\n"
                + "  theFollowingClassesAreTestClasses:\n"
                + testClassesYaml + "\n"
                + "  theFollowingResourceAccessesArePermitted:\n"
                + fsSection
                + networkSection
                + commandSection
                + threadSection
                + packageSection
                + timeoutSection;

        Files.writeString(abs, yaml, StandardCharsets.UTF_8);
        return abs;
    }

    /**
     * Renders the YAML section for file system interaction rules.
     *
     * @param model policy model
     * @return YAML string for the regardingFileSystemInteractions section
     */
    private static String renderFileSystemRules(PolicyDialogModel model) {
        if (model.getFileSystemRules().isEmpty()) {
            return "    regardingFileSystemInteractions: [ ]\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    regardingFileSystemInteractions:\n");
        for (FileSystemRule rule : model.getFileSystemRules()) {
            sb.append("      - onThisPathAndAllPathsBelow: \"").append(escape(rule.getPathAndBelow())).append("\"\n");
            sb.append("        readAllFiles: ").append(rule.isReadAllFiles()).append("\n");
            sb.append("        overwriteAllFiles: ").append(rule.isOverwriteAllFiles()).append("\n");
            sb.append("        executeAllFiles: ").append(rule.isExecuteAllFiles()).append("\n");
            sb.append("        deleteAllFiles: ").append(rule.isDeleteAllFiles()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Renders the YAML section for network connection rules.
     *
     * @param model policy model
     * @return YAML string for the regardingNetworkConnections section
     */
    private static String renderNetworkRules(PolicyDialogModel model) {
        if (model.getNetworkConnectionRules().isEmpty()) {
            return "    regardingNetworkConnections: [ ]\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    regardingNetworkConnections:\n");
        for (NetworkConnectionRule rule : model.getNetworkConnectionRules()) {
            sb.append("      - onTheHost: \"").append(escape(rule.getHost())).append("\"\n");
            sb.append("        onThePort: ").append(rule.getPort()).append("\n");
            sb.append("        openConnections: ").append(rule.isOpenConnections()).append("\n");
            sb.append("        sendData: ").append(rule.isSendData()).append("\n");
            sb.append("        receiveData: ").append(rule.isReceiveData()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Renders the YAML section for command execution rules.
     *
     * @param model policy model
     * @return YAML string for the regardingCommandExecutions section
     */
    private static String renderCommandRules(PolicyDialogModel model) {
        if (model.getCommandExecutionRules().isEmpty()) {
            return "    regardingCommandExecutions: [ ]\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    regardingCommandExecutions:\n");
        for (CommandExecutionRule rule : model.getCommandExecutionRules()) {
            sb.append("      - executeTheCommand: \"").append(escape(rule.getCommand())).append("\"\n");
            if (rule.getArguments().isEmpty()) {
                sb.append("        withTheseArguments: [ ]\n");
            } else {
                sb.append("        withTheseArguments:\n");
                for (String arg : rule.getArguments()) {
                    sb.append("          - \"").append(escape(arg)).append("\"\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Renders the YAML section for thread creation rules.
     *
     * @param model policy model
     * @return YAML string for the regardingThreadCreations section
     */
    private static String renderThreadRules(PolicyDialogModel model) {
        if (model.getThreadCreationRules().isEmpty()) {
            return "    regardingThreadCreations: [ ]\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    regardingThreadCreations:\n");
        for (ThreadCreationRule rule : model.getThreadCreationRules()) {
            sb.append("      - createTheFollowingNumberOfThreads: ").append(rule.getNumberOfThreads()).append("\n");
            sb.append("        ofThisClass: \"").append(escape(rule.getThreadClass())).append("\"\n");
        }
        return sb.toString();
    }

    /**
     * Renders the YAML section for package import rules.
     *
     * @param model policy model
     * @return YAML string for the regardingPackageImports section
     */
    private static String renderPackageRules(PolicyDialogModel model) {
        if (model.getPackageImportRules().isEmpty()) {
            return "    regardingPackageImports: [ ]\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    regardingPackageImports:\n");
        for (PackageImportRule rule : model.getPackageImportRules()) {
            sb.append("      - importTheFollowingPackage: \"").append(escape(rule.getPackageName())).append("\"\n");
        }
        return sb.toString();
    }

    /**
     * Renders the YAML section for timeouts.
     *
     * @param model policy model
     * @return YAML string for the regardingTimeouts section
     */
    private static String renderTimeoutRules(PolicyDialogModel model) {
        if (model.getTimeoutRules().isEmpty()) {
            return "    regardingTimeouts: [ ]\n";
        }

        // Spec: not a list. Either [ ] or a single item.
        // If the UI/model ever contains multiple entries, we intentionally take the first one.
        return "    regardingTimeouts:\n"
                + "      - timeout: " + model.getTimeoutRules().getFirst().getTimeoutSeconds() + "\n";
    }

    /**
     * Escapes backslashes and quote characters for inclusion in a double-quoted YAML string.
     *
     * @param s input string
     * @return escaped string (never {@code null})
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Returns a trimmed value for use in unquoted YAML scalar fields.
     *
     * @param s input string
     * @return trimmed string
     * @throws NullPointerException if {@code s} is {@code null}
     */
    private static String escapePlain(String s) {
        return s.trim();
    }
}
