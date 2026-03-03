package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.PolicyUiConstants;
import de.tum.cit.ase.aresUI.policy.UiSupport;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

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
        grid.setHgap(PolicyUiConstants.GRID_HGAP);
        grid.setVgap(PolicyUiConstants.GRID_HGAP);

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
        c0.setMinWidth(PolicyUiConstants.LABEL_COLUMN_WIDTH);
        c0.setPrefWidth(PolicyUiConstants.LABEL_COLUMN_WIDTH);
        c0.setMaxWidth(PolicyUiConstants.LABEL_COLUMN_WIDTH);
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
     * Loads fields from the given supervised-code metadata values.
     *
     * @param config      programming language configuration identifier
     * @param rootPackage root package of the supervised code
     * @param mainClass   main class name
     * @param testClasses list of test class names (may be {@code null}, treated as empty)
     */
    public void load(String config, String rootPackage, String mainClass, List<String> testClasses) {
        configComboBox.getSelectionModel().select(config);
        rootPackageField.setText(rootPackage);
        mainClassField.setText(mainClass);
        List<String> tests = testClasses == null ? List.of() : testClasses;
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
        return UiSupport.parseLines(getTestClassesRaw());
    }
}
