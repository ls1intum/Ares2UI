package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single file system interaction rule.
 */
final class FileSystemRuleRow {
    final TextField path;
    final Button browse;
    final CheckBox read;
    final CheckBox overwrite;
    final CheckBox create;
    final CheckBox execute;
    final CheckBox delete;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for a file system rule.
     *
     * @param path path field
     * @param browse browse button
     * @param read read checkbox
     * @param overwrite overwrite checkbox
     * @param create create checkbox
     * @param execute execute checkbox
     * @param delete delete checkbox
     * @param remove remove button
     * @param root root grid of the row
     */
    FileSystemRuleRow(TextField path,
                      Button browse,
                      CheckBox read,
                      CheckBox overwrite,
                      CheckBox create,
                      CheckBox execute,
                      CheckBox delete,
                      Button remove,
                      GridPane root) {
        this.path = path;
        this.browse = browse;
        this.read = read;
        this.overwrite = overwrite;
        this.create = create;
        this.execute = execute;
        this.delete = delete;
        this.remove = remove;
        this.root = root;
    }
}
