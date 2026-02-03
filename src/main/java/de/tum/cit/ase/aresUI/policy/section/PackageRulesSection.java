package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.rules.PackageImportRule;
import de.tum.cit.ase.aresUI.policy.UiSupport;
import de.tum.cit.ase.aresUI.policy.row.PackageRuleRow;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI section for regardingPackageImports.
 */
public final class PackageRulesSection {

    private final Button addButton = new Button("Add rule");
    private final VBox container = new VBox(6);
    private final List<PackageRuleRow> rows = new ArrayList<>();

    /**
     * Creates the package rules UI section.
     */
    public PackageRulesSection() {
        addButton.setOnAction(this::onAdd);
    }

    /**
     * Returns the JavaFX node representing this section.
     *
     * @return section root node
     */
    public Node getNode() {
        VBox box = new VBox(6);
        box.getChildren().addAll(new HBox(10, addButton), buildHeaderRow(), container);
        return box;
    }

    /**
     * Loads package import rules into the section.
     *
     * @param rules rules to load (may be {@code null})
     */
    public void load(List<PackageImportRule> rules) {
        rows.clear();
        container.getChildren().clear();
        if (rules == null) return;

        for (PackageImportRule r : rules) {
            PackageRuleRow row = addRowInternal();
            row.pkg.setText(r.getPackageName());
        }
    }

    /**
     * Collects package import rules from the UI.
     *
     * @return list of collected rules
     * @throws IllegalArgumentException if a rule contains invalid data (propagated from {@link PackageImportRule})
     */
    public List<PackageImportRule> collect() {
        return rows.stream()
                .map(r -> new PackageImportRule(r.pkg.getText()))
                .collect(Collectors.toList());
    }

    /**
     * Handles the Add button action.
     *
     * @param ignored action event
     */
    private void onAdd(ActionEvent ignored) {
        addRowInternal();
    }

    /**
     * Creates and adds a new row.
     *
     * @return created row
     */
    private PackageRuleRow addRowInternal() {
        TextField pkg = new TextField();
        pkg.setPromptText("instrumentation.util");

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(pkg, 0, 0);
        row.add(remove, 1, 0);

        PackageRuleRow rr = new PackageRuleRow(pkg, remove, row);
        rows.add(rr);
        container.getChildren().add(row);

        remove.setOnAction(ignored -> onRemove(rr));

        return rr;
    }

    /**
     * Removes the given row.
     *
     * @param rr row to remove
     */
    private void onRemove(PackageRuleRow rr) {
        rows.remove(rr);
        container.getChildren().remove(rr.root);
    }

    /**
     * Creates the column layout used by this section.
     *
     * @return list of columns
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.ALWAYS);
        c0.setMinWidth(420);

        ColumnConstraints c1 = new ColumnConstraints(34);

        return List.of(c0, c1);
    }

    /**
     * Builds the header row.
     *
     * @return header grid
     */
    private GridPane buildHeaderRow() {
        GridPane header = new GridPane();
        header.setHgap(10);
        header.setPadding(UiSupport.headerBottomPadding());
        header.getColumnConstraints().setAll(createColumns());

        header.add(UiSupport.boldLabel("Package"), 0, 0);
        header.add(new Label(""), 1, 0);

        return header;
    }
}
