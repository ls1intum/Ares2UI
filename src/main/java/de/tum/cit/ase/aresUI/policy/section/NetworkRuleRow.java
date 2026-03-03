package de.tum.cit.ase.aresUI.policy.section;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Row editor for a single network connection rule.
 */
final class NetworkRuleRow {
    final TextField host;
    final TextField port;
    final CheckBox open;
    final CheckBox send;
    final CheckBox receive;
    final Button remove;
    final GridPane root;

    /**
     * Creates a row containing the controls for a network rule.
     *
     * @param host host field
     * @param port port field
     * @param open open-connections checkbox
     * @param send send-data checkbox
     * @param receive receive-data checkbox
     * @param remove remove button
     * @param root root grid of the row
     */
    NetworkRuleRow(TextField host, TextField port, CheckBox open, CheckBox send, CheckBox receive, Button remove, GridPane root) {
        this.host = host;
        this.port = port;
        this.open = open;
        this.send = send;
        this.receive = receive;
        this.remove = remove;
        this.root = root;
    }
}
