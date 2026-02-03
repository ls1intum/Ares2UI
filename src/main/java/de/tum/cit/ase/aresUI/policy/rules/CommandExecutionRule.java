package de.tum.cit.ase.aresUI.policy.rules;

import java.util.List;
import java.util.Objects;

/**
 * Represents a rule for executing a command with specified arguments.
 */
public class CommandExecutionRule {

    private final String command;
    private final List<String> arguments;

    /**
     * Creates a command execution rule.
     *
     * @param command command to execute
     * @param arguments command arguments
     * @throws NullPointerException if {@code arguments} is {@code null}
     * @throws IllegalArgumentException if {@code command} is {@code null} or blank
     */
    public CommandExecutionRule(String command, List<String> arguments) {
        this.command = requireNonBlank(command);
        this.arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
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
            throw new IllegalArgumentException("command must not be blank");
        }
        return s.trim();
    }

    /**
     * Returns the command.
     *
     * @return command string
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the command arguments.
     *
     * @return immutable list of arguments
     */
    public List<String> getArguments() {
        return arguments;
    }
}
