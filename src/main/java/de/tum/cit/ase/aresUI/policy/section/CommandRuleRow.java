package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single command execution rule.
 */
final class CommandRuleRow {
    final TextField command;
    final TextField argsRaw;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for a command rule.
     *
     * @param command command text field
     * @param argsRaw arguments text field
     * @param remove remove button
     * @param root root grid of the row
     */
    CommandRuleRow(TextField command, TextField argsRaw, Button remove, GridPane root) {
        this.command = command;
        this.argsRaw = argsRaw;
        this.remove = remove;
        this.root = root;
    }
}
