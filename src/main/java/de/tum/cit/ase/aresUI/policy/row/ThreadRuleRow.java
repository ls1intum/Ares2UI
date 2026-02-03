package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single thread creation rule.
 */
public final class ThreadRuleRow {
    public final TextField count;
    public final TextField clazz;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for a thread creation rule.
     *
     * @param count thread count field
     * @param clazz thread class field
     * @param remove remove button
     * @param root root grid of the row
     */
    public ThreadRuleRow(TextField count, TextField clazz, Button remove, GridPane root) {
        this.count = count;
        this.clazz = clazz;
        this.remove = remove;
        this.root = root;
    }
}
