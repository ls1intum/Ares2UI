package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single command execution rule.
 */
public final class CommandRuleRow {
    public final TextField command;
    public final TextField argsRaw;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for a command rule.
     *
     * @param command command text field
     * @param argsRaw arguments text field
     * @param remove remove button
     * @param root root grid of the row
     */
    public CommandRuleRow(TextField command, TextField argsRaw, Button remove, GridPane root) {
        this.command = command;
        this.argsRaw = argsRaw;
        this.remove = remove;
        this.root = root;
    }
}
