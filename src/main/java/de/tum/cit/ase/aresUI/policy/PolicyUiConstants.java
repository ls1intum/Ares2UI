package de.tum.cit.ase.aresUI.policy;

/**
 * Shared UI constants for the policy editor components.
 *
 * <p>Centralizes column widths, CSS style strings, spacing values, and label texts
 * that are repeated across section and dialog classes.
 */
public final class PolicyUiConstants {

    private PolicyUiConstants() {
    }

    // ── Column widths ──────────────────────────────────────────────────

    /** Width of the remove-button column used by all rule sections. */
    public static final double REMOVE_COLUMN_WIDTH = 34;

    /** Width of the label column in the supervised-code section. */
    public static final double LABEL_COLUMN_WIDTH = 190;

    // ── Spacing ────────────────────────────────────────────────────────

    /** Horizontal gap between grid columns. */
    public static final double GRID_HGAP = 10;

    /** Bottom padding for section header rows. */
    public static final double HEADER_BOTTOM_PADDING = 4;

    /** Padding used inside the dialog form and around bottom buttons. */
    public static final double DIALOG_PADDING = 12;

    // ── Remove button ──────────────────────────────────────────────────

    /** Fixed width of the remove ("✕") button. */
    public static final double REMOVE_BUTTON_WIDTH = 28;

    // ── Inline CSS styles ──────────────────────────────────────────────

    /** Style applied to bold header labels. */
    public static final String STYLE_BOLD = "-fx-font-weight: bold;";

    /** Style applied to the error label in the dialog. */
    public static final String STYLE_ERROR_TEXT = "-fx-text-fill: #b00020;";

    // ── Dialog / section labels ────────────────────────────────────────

    /** Text for the Add-rule button shown in every rule section. */
    public static final String ADD_RULE_LABEL = "Add rule";

    /** Title of the policy dialog window. */
    public static final String DIALOG_TITLE = "Create Security Policy";

    /** Label for the Save button. */
    public static final String SAVE_LABEL = "Save";

    /** Label for the Cancel button. */
    public static final String CANCEL_LABEL = "Cancel";
}
