package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.dialog.PolicyDialogModel;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * UI section for supervised-code metadata: programming language configuration, root package,
 * main class, and test classes.
 */
public final class SupervisedCodeSection {

    private final GridPane grid = new GridPane();

    private final ComboBox<String> configComboBox = new ComboBox<>();
    private final TextField rootPackageField = new TextField();
    private final TextField mainClassField = new TextField();
    private final TextArea testClassesArea = new TextArea();

    /**
     * Creates the supervised-code metadata section with default values.
     */
    public SupervisedCodeSection() {
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        grid.add(new Label("Programming Language Config:"), 0, row);
        configComboBox.getItems().setAll(List.of(
                "JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ",
                "JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION",
                "JAVA_USING_MAVEN_WALA_AND_ASPECTJ",
                "JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION",
                "JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ",
                "JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION",
                "JAVA_USING_GRADLE_WALA_AND_ASPECTJ",
                "JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION"
        ));
        configComboBox.getSelectionModel().select("JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION");
        grid.add(configComboBox, 1, row++);

        grid.add(new Label("Root package:"), 0, row);
        rootPackageField.setText("org.example");
        grid.add(rootPackageField, 1, row++);

        grid.add(new Label("Main class:"), 0, row);
        mainClassField.setText("Main");
        grid.add(mainClassField, 1, row++);

        grid.add(new Label("Test classes (one per line):"), 0, row);
        testClassesArea.setPrefRowCount(4);
        testClassesArea.setText("org.example.PenguinTest");
        testClassesArea.setPrefHeight(90);
        testClassesArea.setMinHeight(80);
        grid.add(testClassesArea, 1, row);

        GridPane.setHgrow(configComboBox, Priority.ALWAYS);
        GridPane.setHgrow(rootPackageField, Priority.ALWAYS);
        GridPane.setHgrow(mainClassField, Priority.ALWAYS);
        GridPane.setHgrow(testClassesArea, Priority.ALWAYS);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(190);
        c0.setPrefWidth(190);
        c0.setMaxWidth(190);
        c0.setHgrow(Priority.NEVER);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().setAll(c0, c1);
    }

    /**
     * Returns the JavaFX node representing this section.
     *
     * @return section root node
     */
    public Node getNode() {
        return grid;
    }

    /**
     * Loads fields from an existing policy model.
     * Missing lists are treated as empty lists.
     *
     * @param model policy model to load
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public void load(PolicyDialogModel model) {
        Objects.requireNonNull(model, "model");
        configComboBox.getSelectionModel().select(model.getProgrammingLanguageConfiguration());
        rootPackageField.setText(model.getRootPackage());
        mainClassField.setText(model.getMainClass());
        List<String> tests = model.getTestClasses() == null ? List.of() : model.getTestClasses();
        testClassesArea.setText(String.join("\n", tests));
    }

    /**
     * Returns the program configuration currently selected in the combo box.
     *
     * @return selected configuration identifier
     */
    public String getSelectedConfig() {
        return configComboBox.getValue();
    }

    /**
     * Returns the root package field value.
     *
     * @return root package text
     */
    public String getRootPackage() {
        return rootPackageField.getText();
    }

    /**
     * Returns the main class field value.
     *
     * @return main class text
     */
    public String getMainClass() {
        return mainClassField.getText();
    }

    /**
     * Returns the raw multi-line test class text.
     *
     * @return raw text
     */
    public String getTestClassesRaw() {
        return testClassesArea.getText();
    }

    /**
     * Parses the raw test class text into a list of non-empty class names.
     *
     * @return list of test class names
     */
    public List<String> getTestClasses() {
        String raw = getTestClassesRaw();
        if (raw == null) return List.of();
        return Arrays.stream(raw.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
