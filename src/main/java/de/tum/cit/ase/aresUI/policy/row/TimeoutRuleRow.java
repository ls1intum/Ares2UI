package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for the (single) timeout rule.
 */
public final class TimeoutRuleRow {
    public final TextField seconds;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for the timeout rule.
     *
     * @param seconds timeout seconds field
     * @param remove remove button
     * @param root root grid of the row
     */
    public TimeoutRuleRow(TextField seconds, Button remove, GridPane root) {
        this.seconds = seconds;
        this.remove = remove;
        this.root = root;
    }
}
