package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.PolicyUiConstants;
import de.tum.cit.ase.aresUI.policy.rules.ThreadCreationRule;
import de.tum.cit.ase.aresUI.policy.UiSupport;
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
 * UI section for regardingThreadCreations.
 */
public final class ThreadRulesSection {

    private final Button addButton = new Button(PolicyUiConstants.ADD_RULE_LABEL);
    private final VBox container = new VBox(6);
    private final List<ThreadRuleRow> rows = new ArrayList<>();

    /**
     * Creates the thread rules UI section.
     */
    public ThreadRulesSection() {
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
     * Loads thread creation rules into the section.
     *
     * @param rules rules to load (may be {@code null})
     */
    public void load(List<ThreadCreationRule> rules) {
        rows.clear();
        container.getChildren().clear();
        if (rules == null) return;

        for (ThreadCreationRule r : rules) {
            ThreadRuleRow row = addRowInternal();
            row.count.setText(String.valueOf(r.getNumberOfThreads()));
            row.clazz.setText(r.getThreadClass());
        }
    }

    /**
     * Collects thread creation rules from the UI.
     *
     * @return list of collected rules
     * @throws IllegalArgumentException if any rule contains invalid data (propagated from {@link ThreadCreationRule})
     */
    public List<ThreadCreationRule> collect() {
        return rows.stream()
                .map(r -> new ThreadCreationRule(UiSupport.parseIntOrZero(r.count.getText()), r.clazz.getText()))
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
     * Creates and adds a new thread rule row.
     *
     * @return created row
     */
    private ThreadRuleRow addRowInternal() {
        TextField count = new TextField();
        count.setPromptText("10");

        TextField clazz = new TextField();
        clazz.setPromptText("instrumentation.lang.Thread");

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(count, 0, 0);
        row.add(clazz, 1, 0);
        row.add(remove, 2, 0);

        ThreadRuleRow rr = new ThreadRuleRow(count, clazz, remove, row);
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
    private void onRemove(ThreadRuleRow rr) {
        rows.remove(rr);
        container.getChildren().remove(rr.root);
    }

    /**
     * Creates the column layout for this section.
     *
     * @return list of columns
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints(140);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMinWidth(380);

        ColumnConstraints c2 = new ColumnConstraints(PolicyUiConstants.REMOVE_COLUMN_WIDTH);

        return List.of(c0, c1, c2);
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

        header.add(UiSupport.boldLabel("#Threads"), 0, 0);
        header.add(UiSupport.boldLabel("Class"), 1, 0);
        header.add(new Label(""), 2, 0);

        return header;
    }
}
