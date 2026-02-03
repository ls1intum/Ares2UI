package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.rules.NetworkConnectionRule;
import de.tum.cit.ase.aresUI.policy.UiSupport;
import de.tum.cit.ase.aresUI.policy.row.NetworkRuleRow;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * UI section for regardingNetworkConnections.
 */
public final class NetworkRulesSection {

    private final Button addButton = new Button("Add rule");
    private final VBox container = new VBox(6);
    private final List<NetworkRuleRow> rows = new ArrayList<>();

    /**
     * Creates the network rules UI section.
     */
    public NetworkRulesSection() {
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
     * Loads network connection rules into the section.
     *
     * @param rules rules to load (may be {@code null})
     */
    public void load(List<NetworkConnectionRule> rules) {
        rows.clear();
        container.getChildren().clear();
        if (rules == null) return;

        for (NetworkConnectionRule r : rules) {
            NetworkRuleRow row = addRowInternal();
            row.host.setText(r.getHost());
            row.port.setText(String.valueOf(r.getPort()));
            row.open.setSelected(r.isOpenConnections());
            row.send.setSelected(r.isSendData());
            row.receive.setSelected(r.isReceiveData());
        }
    }

    /**
     * Collects network connection rules from the UI.
     *
     * @return list of collected rules
     * @throws IllegalArgumentException if any rule contains invalid data (propagated from {@link NetworkConnectionRule})
     */
    public List<NetworkConnectionRule> collect() {
        return rows.stream()
                .map(r -> new NetworkConnectionRule(
                        r.host.getText(),
                        UiSupport.parseIntOrZero(r.port.getText()),
                        r.open.isSelected(),
                        r.send.isSelected(),
                        r.receive.isSelected()
                ))
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
    private NetworkRuleRow addRowInternal() {
        TextField host = new TextField();
        host.setPromptText("e.g. www.example.com");

        TextField port = new TextField();
        port.setPromptText("80");

        CheckBox open = new CheckBox();
        CheckBox send = new CheckBox();
        CheckBox receive = new CheckBox();

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(host, 0, 0);
        row.add(port, 1, 0);
        row.add(open, 2, 0);
        row.add(send, 3, 0);
        row.add(receive, 4, 0);
        row.add(remove, 5, 0);

        NetworkRuleRow rr = new NetworkRuleRow(host, port, open, send, receive, remove, row);
        rows.add(rr);
        container.getChildren().add(row);

        remove.setOnAction(ignored -> onRemove(rr));

        return rr;
    }

    /**
     * Removes the given row from the UI.
     *
     * @param rr row to remove
     */
    private void onRemove(NetworkRuleRow rr) {
        rows.remove(rr);
        container.getChildren().remove(rr.root);
    }

    /**
     * Creates the column layout used by this section.
     *
     * @return list of column constraints
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.ALWAYS);
        c0.setMinWidth(280);

        ColumnConstraints c1 = new ColumnConstraints(80);
        ColumnConstraints c2 = new ColumnConstraints(120);
        ColumnConstraints c3 = new ColumnConstraints(90);
        ColumnConstraints c4 = new ColumnConstraints(110);
        ColumnConstraints c5 = new ColumnConstraints(34);

        return List.of(c0, c1, c2, c3, c4, c5);
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

        header.add(UiSupport.boldLabel("Host"), 0, 0);
        header.add(UiSupport.boldLabel("Port"), 1, 0);
        header.add(UiSupport.boldLabel("Open"), 2, 0);
        header.add(UiSupport.boldLabel("Send"), 3, 0);
        header.add(UiSupport.boldLabel("Receive"), 4, 0);
        header.add(new Label(""), 5, 0);

        return header;
    }
}
