package de.tum.cit.ase.aresUI.policy;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Small UI helper utilities shared by policy editor components.
 */
public final class UiSupport {
    /**
     * Prevents instantiation.
     */
    private UiSupport() {
    }

    /**
     * Creates a small remove button appropriate for rule rows.
     *
     * @return configured remove button
     */
    public static Button createRemoveButton() {
        Button remove = new Button("✕");
        remove.setFocusTraversable(false);
        remove.setMinWidth(PolicyUiConstants.REMOVE_BUTTON_WIDTH);
        remove.setPrefWidth(PolicyUiConstants.REMOVE_BUTTON_WIDTH);
        remove.setMaxWidth(PolicyUiConstants.REMOVE_BUTTON_WIDTH);
        remove.setTooltip(new Tooltip("Remove rule"));
        return remove;
    }

    /**
     * Creates a label with bold font weight.
     *
     * @param text label text
     * @return configured label
     */
    public static Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(PolicyUiConstants.STYLE_BOLD);
        return l;
    }

    /**
     * Returns a standard bottom padding used for section header rows.
     *
     * @return padding insets
     */
    public static Insets headerBottomPadding() {
        return new Insets(0, 0, PolicyUiConstants.HEADER_BOTTOM_PADDING, 0);
    }

    /**
     * Splits a raw command line argument string into individual arguments.
     *
     * <p>Arguments are separated by whitespace unless enclosed in double quotes. Quote characters are not
     * included in the returned arguments.
     *
     * @param raw raw argument string (may be {@code null})
     * @return list of parsed arguments
     */
    public static List<String> splitArgs(String raw) {
        if (raw == null) return List.of();
        String t = raw.trim();
        if (t.isEmpty()) return List.of();

        List<String> args = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            args.add(current.toString());
        }
        return args;
    }

    /**
     * Parses an integer from the given string.
     *
     * @param s input string (may be {@code null})
     * @return parsed integer, or {@code 0} when the input is blank or not a valid integer
     */
    public static int parseIntOrZero(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty()) return 0;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Splits a raw multi-line string into a list of non-empty, trimmed lines.
     *
     * @param raw raw text (may be {@code null})
     * @return list of trimmed, non-empty lines; empty list when {@code raw} is {@code null} or blank
     */
    public static List<String> parseLines(String raw) {
        if (raw == null) return List.of();
        return Arrays.stream(raw.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
