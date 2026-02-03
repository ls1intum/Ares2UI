package de.tum.cit.ase.aresUI.policy.row;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single network connection rule.
 */
public final class NetworkRuleRow {
    public final TextField host;
    public final TextField port;
    public final CheckBox open;
    public final CheckBox send;
    public final CheckBox receive;
    public final Button remove;
    public final GridPane root;

    /**
     * Creates a row view-model containing the controls for a network rule.
     *
     * @param host host field
     * @param port port field
     * @param open open-connections checkbox
     * @param send send-data checkbox
     * @param receive receive-data checkbox
     * @param remove remove button
     * @param root root grid of the row
     */
    public NetworkRuleRow(TextField host, TextField port, CheckBox open, CheckBox send, CheckBox receive, Button remove, GridPane root) {
        this.host = host;
        this.port = port;
        this.open = open;
        this.send = send;
        this.receive = receive;
        this.remove = remove;
        this.root = root;
    }
}
