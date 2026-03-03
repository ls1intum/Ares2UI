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

import java.util.List;
import java.util.Objects;

/**
 * Maps between {@link PolicyDialogModel} and the policy editor UI sections.
 *
 * <p>Null-safety / legacy behavior: missing lists are treated as empty lists and missing primitive-ish fields
 * are replaced by safe defaults, allowing older policies to be loaded and edited.
 */
public final class PolicyDialogBinder {

    private final SupervisedCodeSection supervisedCode;
    private final FileSystemRulesSection fileSystem;
    private final NetworkRulesSection network;
    private final CommandRulesSection commands;
    private final ThreadRulesSection threads;
    private final PackageRulesSection packages;
    private final TimeoutSection timeout;

    /**
     * Creates a binder for the given UI sections.
     *
     * @param supervisedCode section for supervised-code metadata
     * @param fileSystem section for file system rules
     * @param network section for network rules
     * @param commands section for command execution rules
     * @param threads section for thread creation rules
     * @param packages section for package import rules
     * @param timeout section for timeout rules
     * @throws NullPointerException if any section is {@code null}
     */
    public PolicyDialogBinder(SupervisedCodeSection supervisedCode,
                              FileSystemRulesSection fileSystem,
                              NetworkRulesSection network,
                              CommandRulesSection commands,
                              ThreadRulesSection threads,
                              PackageRulesSection packages,
                              TimeoutSection timeout) {
        this.supervisedCode = Objects.requireNonNull(supervisedCode, "supervisedCode");
        this.fileSystem = Objects.requireNonNull(fileSystem, "fileSystem");
        this.network = Objects.requireNonNull(network, "network");
        this.commands = Objects.requireNonNull(commands, "commands");
        this.threads = Objects.requireNonNull(threads, "threads");
        this.packages = Objects.requireNonNull(packages, "packages");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    /**
     * Loads model values into the UI.
     *
     * @param model source model
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public void loadFromModel(PolicyDialogModel model) {
        Objects.requireNonNull(model, "model");

        supervisedCode.load(
                model.getProgrammingLanguageConfiguration(),
                model.getRootPackage(),
                model.getMainClass(),
                model.getTestClasses()
        );
        fileSystem.load(nullToEmpty(model.getFileSystemRules()));
        network.load(nullToEmpty(model.getNetworkConnectionRules()));
        commands.load(nullToEmpty(model.getCommandExecutionRules()));
        threads.load(nullToEmpty(model.getThreadCreationRules()));
        packages.load(nullToEmpty(model.getPackageImportRules()));
        timeout.load(nullToEmpty(model.getTimeoutRules()));
    }

    /**
     * Collects the current UI state into a {@link PolicyDialogModel}.
     *
     * @return collected model
     */
    public PolicyDialogModel collectToModel() {
        String config = supervisedCode.getSelectedConfig();
        String rootPackage = supervisedCode.getRootPackage();
        String mainClass = supervisedCode.getMainClass();
        List<String> testClasses = supervisedCode.getTestClasses();

        List<FileSystemRule> fs = fileSystem.collect();
        List<NetworkConnectionRule> net = network.collect();
        List<CommandExecutionRule> cmd = commands.collect();
        List<ThreadCreationRule> thr = threads.collect();
        List<PackageImportRule> pkg = packages.collect();
        List<TimeoutRule> to = timeout.collect();

        return new PolicyDialogModel(config, rootPackage, mainClass, testClasses, fs, net, cmd, thr, pkg, to);
    }

    /**
     * Returns an empty list when {@code list} is {@code null}.
     *
     * @param list input list
     * @return {@code list} or an empty list when {@code null}
     * @param <T> element type
     */
    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
