package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.PolicyUiConstants;
import de.tum.cit.ase.aresUI.policy.rules.CommandExecutionRule;
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
 * UI section for regardingCommandExecutions.
 */
public final class CommandRulesSection {

    private final Button addButton = new Button(PolicyUiConstants.ADD_RULE_LABEL);
    private final VBox container = new VBox(6);
    private final List<CommandRuleRow> rows = new ArrayList<>();

    /**
     * Creates the command rules UI section.
     */
    public CommandRulesSection() {
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
     * Loads command execution rules into the section.
     *
     * @param rules rules to load (may be {@code null})
     */
    public void load(List<CommandExecutionRule> rules) {
        rows.clear();
        container.getChildren().clear();
        if (rules == null) return;

        for (CommandExecutionRule r : rules) {
            CommandRuleRow row = addRowInternal();
            row.command.setText(r.getCommand());
            row.argsRaw.setText(String.join(" ", r.getArguments()));
        }
    }

    /**
     * Collects command execution rules from the UI.
     *
     * @return list of collected rules
     * @throws IllegalArgumentException if any rule contains invalid data (propagated from {@link CommandExecutionRule})
     */
    public List<CommandExecutionRule> collect() {
        return rows.stream()
                .map(r -> new CommandExecutionRule(r.command.getText(), UiSupport.splitArgs(r.argsRaw.getText())))
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
     * Creates and adds a new command rule row.
     *
     * @return created row
     */
    private CommandRuleRow addRowInternal() {
        TextField command = new TextField();
        command.setPromptText("e.g. ls");

        TextField args = new TextField();
        args.setPromptText("args separated by spaces, or quote with \"\". e.g. -l or \"foo bar\"");

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(command, 0, 0);
        row.add(args, 1, 0);
        row.add(remove, 2, 0);

        CommandRuleRow rr = new CommandRuleRow(command, args, remove, row);
        rows.add(rr);
        container.getChildren().add(row);

        remove.setOnAction(e -> onRemove(rr));

        return rr;
    }

    /**
     * Removes the given row.
     *
     * @param rr row to remove
     */
    private void onRemove(CommandRuleRow rr) {
        rows.remove(rr);
        container.getChildren().remove(rr.root);
    }

    /**
     * Creates column constraints for the command rules grid.
     *
     * @return list of columns
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints(200);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMinWidth(320);

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

        header.add(UiSupport.boldLabel("Command"), 0, 0);
        header.add(UiSupport.boldLabel("Arguments"), 1, 0);
        header.add(new Label(""), 2, 0);

        return header;
    }
}
