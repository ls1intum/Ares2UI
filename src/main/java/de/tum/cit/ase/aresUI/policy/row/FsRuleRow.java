package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single file system interaction rule.
 */
public final class FsRuleRow {
    public final TextField path;
    public final Button browse;
    public final CheckBox read;
    public final CheckBox overwrite;
    public final CheckBox create;
    public final CheckBox execute;
    public final CheckBox delete;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for a file system rule.
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
    public FsRuleRow(TextField path,
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
