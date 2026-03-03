package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for the (single) timeout rule.
 */
final class TimeoutRuleRow {
    final TextField seconds;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for the timeout rule.
     *
     * @param seconds timeout seconds field
     * @param remove remove button
     * @param root root grid of the row
     */
    TimeoutRuleRow(TextField seconds, Button remove, GridPane root) {
        this.seconds = seconds;
        this.remove = remove;
        this.root = root;
    }
}
