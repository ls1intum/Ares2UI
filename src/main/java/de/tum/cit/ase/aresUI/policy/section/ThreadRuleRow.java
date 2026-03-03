package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single thread creation rule.
 */
final class ThreadRuleRow {
    final TextField count;
    final TextField clazz;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for a thread creation rule.
     *
     * @param count thread count field
     * @param clazz thread class field
     * @param remove remove button
     * @param root root grid of the row
     */
    ThreadRuleRow(TextField count, TextField clazz, Button remove, GridPane root) {
        this.count = count;
        this.clazz = clazz;
        this.remove = remove;
        this.root = root;
    }
}
