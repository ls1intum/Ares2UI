package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single package import rule.
 */
final class PackageRuleRow {
    final TextField pkg;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for a package import rule.
     *
     * @param pkg package name field
     * @param remove remove button
     * @param root root grid of the row
     */
    PackageRuleRow(TextField pkg, Button remove, GridPane root) {
        this.pkg = pkg;
        this.remove = remove;
        this.root = root;
    }
}
