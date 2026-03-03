package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.PolicyUiConstants;
import de.tum.cit.ase.aresUI.policy.rules.TimeoutRule;
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

/**
 * UI section for regardingTimeouts.
 *
 * <p>Spec: at most one timeout entry. Missing/empty list means no timeout.
 */
public final class TimeoutSection {

    private final Button addTimeoutRuleButton = new Button(PolicyUiConstants.ADD_RULE_LABEL);
    private final VBox container = new VBox(6);
    private final List<TimeoutRuleRow> rows = new ArrayList<>();

    /**
     * Creates the timeout UI section.
     */
    public TimeoutSection() {
        addTimeoutRuleButton.setOnAction(this::onAdd);
        syncAddButtonState();
    }

    /**
     * Returns the JavaFX node representing this section.
     *
     * @return section root node
     */
    public Node getNode() {
        VBox box = new VBox(6);
        box.getChildren().addAll(buildTopBar(), buildHeaderRow(), container);
        return box;
    }

    /**
     * Loads timeout rules into the section.
     *
     * <p>Only the first entry is loaded; additional entries are ignored.
     *
     * @param rules timeout rules (may be {@code null})
     */
    public void load(List<TimeoutRule> rules) {
        rows.clear();
        container.getChildren().clear();

        if (rules == null || rules.isEmpty()) {
            syncAddButtonState();
            return;
        }

        TimeoutRule r = rules.get(0);
        TimeoutRuleRow row = addRowInternal();
        row.seconds.setText(String.valueOf(r.getTimeoutSeconds()));
        syncAddButtonState();
    }

    /**
     * Collects timeout rules from the UI.
     *
     * @return list of timeout rules (may be empty)
     * @throws IllegalArgumentException if a row contains a non-positive timeout
     */
    public List<TimeoutRule> collect() {
        return rows.stream()
                .map(r -> new TimeoutRule(UiSupport.parseIntOrZero(r.seconds.getText())))
                .toList();
    }

    /**
     * Builds the top bar containing the Add button.
     *
     * @return top bar node
     */
    private HBox buildTopBar() {
        return new HBox(10, addTimeoutRuleButton);
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
        header.add(UiSupport.boldLabel("Seconds"), 0, 0);
        header.add(new Label(""), 1, 0);
        return header;
    }

    /**
     * Creates column constraints for the timeout rule grid.
     *
     * @return list of column constraints
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints(200);
        ColumnConstraints c1 = new ColumnConstraints(PolicyUiConstants.REMOVE_COLUMN_WIDTH);
        c0.setHgrow(Priority.NEVER);
        return List.of(c0, c1);
    }

    /**
     * Handles the Add button action.
     *
     * @param ignored action event
     */
    private void onAdd(ActionEvent ignored) {
        addRow();
    }

    /**
     * Adds a timeout row if none exists.
     */
    private void addRow() {
        if (!rows.isEmpty()) {
            syncAddButtonState();
            return;
        }
        addRowInternal();
        syncAddButtonState();
    }

    /**
     * Creates and adds a new timeout row.
     *
     * @return the created row
     */
    private TimeoutRuleRow addRowInternal() {
        TextField seconds = new TextField();
        seconds.setPromptText("120");

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(seconds, 0, 0);
        row.add(remove, 1, 0);

        TimeoutRuleRow rr = new TimeoutRuleRow(seconds, remove, row);
        rows.add(rr);
        container.getChildren().add(row);

        remove.setOnAction(this::onRemove);
        remove.setUserData(rr);

        return rr;
    }

    /**
     * Removes the timeout row associated with the remove button.
     *
     * @param e action event
     */
    private void onRemove(ActionEvent e) {
        Object ud = ((Button) e.getSource()).getUserData();
        if (!(ud instanceof TimeoutRuleRow rr)) return;

        rows.remove(rr);
        container.getChildren().remove(rr.root);
        syncAddButtonState();
    }

    /**
     * Updates the enabled/disabled state of the Add button.
     */
    private void syncAddButtonState() {
        addTimeoutRuleButton.setDisable(!rows.isEmpty());
    }
}
