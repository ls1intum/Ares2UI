package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single package import rule.
 */
public final class PackageRuleRow {
    public final TextField pkg;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for a package import rule.
     *
     * @param pkg package name field
     * @param remove remove button
     * @param root root grid of the row
     */
    public PackageRuleRow(TextField pkg, Button remove, GridPane root) {
        this.pkg = pkg;
        this.remove = remove;
        this.root = root;
    }
}
