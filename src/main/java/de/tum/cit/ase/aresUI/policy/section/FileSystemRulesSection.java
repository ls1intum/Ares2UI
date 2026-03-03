package de.tum.cit.ase.aresUI.policy.section;

import de.tum.cit.ase.aresUI.policy.PolicyUiConstants;
import de.tum.cit.ase.aresUI.policy.rules.FileSystemRule;
import de.tum.cit.ase.aresUI.policy.UiSupport;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * UI section for regardingFileSystemInteractions.
 */
public final class FileSystemRulesSection {

    private final Stage stage;

    private final Button addButton = new Button(PolicyUiConstants.ADD_RULE_LABEL);
    private final VBox container = new VBox(6);
    private final List<FileSystemRuleRow> rows = new ArrayList<>();

    /**
     * Creates the file system rules UI section.
     *
     * @param stage owning stage used for file and directory chooser dialogs
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public FileSystemRulesSection(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
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
     * Loads file system rules into the section.
     *
     * @param rules rules to load (may be {@code null})
     */
    public void load(List<FileSystemRule> rules) {
        rows.clear();
        container.getChildren().clear();
        if (rules == null) return;

        for (FileSystemRule r : rules) {
            FileSystemRuleRow row = addRowInternal();
            row.path.setText(r.getPathAndBelow());
            row.read.setSelected(r.isReadAllFiles());
            row.overwrite.setSelected(r.isOverwriteAllFiles());
            row.create.setSelected(r.isCreateAllFiles());
            row.execute.setSelected(r.isExecuteAllFiles());
            row.delete.setSelected(r.isDeleteAllFiles());
        }
    }

    /**
     * Collects file system rules from the UI.
     *
     * @return list of collected rules
     * @throws IllegalArgumentException if any rule contains invalid data (propagated from {@link FileSystemRule})
     */
    public List<FileSystemRule> collect() {
        return rows.stream()
                .map(r -> new FileSystemRule(
                        r.path.getText(),
                        r.read.isSelected(),
                        r.overwrite.isSelected(),
                        r.create.isSelected(),
                        r.execute.isSelected(),
                        r.delete.isSelected()
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
     * Creates and adds a new file system rule row.
     *
     * @return created row
     */
    private FileSystemRuleRow addRowInternal() {
        TextField path = new TextField();
        path.setPromptText("e.g. something.txt");

        Button browse = new Button("…");
        browse.setFocusTraversable(false);
        browse.setMinWidth(28);
        browse.setPrefWidth(28);
        browse.setMaxWidth(28);
        browse.setTooltip(new Tooltip("Select file(s)/folder"));

        CheckBox read = new CheckBox();
        CheckBox overwrite = new CheckBox();
        CheckBox create = new CheckBox();
        CheckBox execute = new CheckBox();
        CheckBox delete = new CheckBox();

        Button remove = UiSupport.createRemoveButton();

        GridPane row = new GridPane();
        row.setHgap(10);
        row.getColumnConstraints().setAll(createColumns());

        row.add(path, 0, 0);
        row.add(browse, 1, 0);
        row.add(read, 2, 0);
        row.add(overwrite, 3, 0);
        row.add(create, 4, 0);
        row.add(execute, 5, 0);
        row.add(delete, 6, 0);
        row.add(remove, 7, 0);

        FileSystemRuleRow rr = new FileSystemRuleRow(path, browse, read, overwrite, create, execute, delete, remove, row);
        rows.add(rr);
        container.getChildren().add(row);

        browse.setOnAction(this::onBrowse);
        browse.setUserData(rr);

        remove.setOnAction(this::onRemove);
        remove.setUserData(rr);

        return rr;
    }

    /**
     * Opens a path chooser context menu for the row associated with the browse button.
     *
     * @param e action event
     */
    private void onBrowse(ActionEvent e) {
        Object ud = ((Button) e.getSource()).getUserData();
        if (ud instanceof FileSystemRuleRow rr) {
            openPathChooser(rr);
        }
    }

    /**
     * Removes the row associated with the remove button.
     *
     * @param e action event
     */
    private void onRemove(ActionEvent e) {
        Object ud = ((Button) e.getSource()).getUserData();
        if (!(ud instanceof FileSystemRuleRow rr)) return;

        rows.remove(rr);
        container.getChildren().remove(rr.root);
    }

    /**
     * Shows a context menu to select either files or a folder and applies the selection to the target row.
     *
     * @param targetRow row to update
     * @throws NullPointerException if {@code targetRow} is {@code null}
     */
    private void openPathChooser(FileSystemRuleRow targetRow) {
        Objects.requireNonNull(targetRow, "targetRow");

        ContextMenu menu = new ContextMenu();

        MenuItem selectFiles = new MenuItem("Select file(s)…");
        selectFiles.setOnAction(this::onSelectFilesMenuItem);
        selectFiles.setUserData(targetRow);

        MenuItem selectFolder = new MenuItem("Select folder…");
        selectFolder.setOnAction(this::onSelectFolderMenuItem);
        selectFolder.setUserData(targetRow);

        menu.getItems().addAll(selectFiles, selectFolder);
        menu.show(targetRow.browse, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    /**
     * Handles the "Select file(s)" action and applies selected paths.
     *
     * @param e action event
     */
    private void onSelectFilesMenuItem(ActionEvent e) {
        Object ud = ((MenuItem) e.getSource()).getUserData();
        if (!(ud instanceof FileSystemRuleRow targetRow)) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select files");
        setInitialChooserDirectoryIfPossible(chooser, null, targetRow.path.getText());

        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null || files.isEmpty()) {
            return;
        }

        applySelectedPaths(files.stream().map(File::toPath).map(Path::toString).collect(Collectors.toList()), targetRow);
    }

    /**
     * Handles the "Select folder" action and applies the selected folder path.
     *
     * @param e action event
     */
    private void onSelectFolderMenuItem(ActionEvent e) {
        Object ud = ((MenuItem) e.getSource()).getUserData();
        if (!(ud instanceof FileSystemRuleRow targetRow)) return;

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder");
        setInitialChooserDirectoryIfPossible(null, chooser, targetRow.path.getText());

        File dir = chooser.showDialog(stage);
        if (dir == null) {
            return;
        }

        applySelectedPaths(List.of(dir.toPath().toString()), targetRow);
    }

    /**
     * Applies selected paths to the given row.
     *
     * <p>The first path is written into {@code targetRow}. Additional paths create additional rows that
     * copy the permission flags from the target row.
     *
     * @param selectedPaths selected path strings
     * @param targetRow target row
     */
    private void applySelectedPaths(List<String> selectedPaths, FileSystemRuleRow targetRow) {
        if (selectedPaths == null || selectedPaths.isEmpty()) return;

        targetRow.path.setText(selectedPaths.get(0));

        for (int i = 1; i < selectedPaths.size(); i++) {
            FileSystemRuleRow newRow = addRowInternal();
            newRow.path.setText(selectedPaths.get(i));

            newRow.read.setSelected(targetRow.read.isSelected());
            newRow.overwrite.setSelected(targetRow.overwrite.isSelected());
            newRow.create.setSelected(targetRow.create.isSelected());
            newRow.execute.setSelected(targetRow.execute.isSelected());
            newRow.delete.setSelected(targetRow.delete.isSelected());
        }
    }

    /**
     * Attempts to set an initial directory on a file chooser or directory chooser based on the current text.
     *
     * @param fileChooser file chooser to configure (may be {@code null})
     * @param directoryChooser directory chooser to configure (may be {@code null})
     * @param currentPathText current text field value
     */
    private void setInitialChooserDirectoryIfPossible(FileChooser fileChooser, DirectoryChooser directoryChooser, String currentPathText) {
        try {
            if (currentPathText == null || currentPathText.trim().isEmpty()) {
                return;
            }

            File f = new File(currentPathText.trim());
            File dir = f.isDirectory() ? f : f.getParentFile();
            if (dir != null && dir.exists() && dir.isDirectory()) {
                if (fileChooser != null) {
                    fileChooser.setInitialDirectory(dir);
                }
                if (directoryChooser != null) {
                    directoryChooser.setInitialDirectory(dir);
                }
            }
        } catch (Exception ignored) {
            // ignore invalid paths
        }
    }

    /**
     * Creates the column layout used by this section.
     *
     * @return list of column constraints
     */
    private static List<ColumnConstraints> createColumns() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.ALWAYS);
        c0.setMinWidth(240);

        ColumnConstraints cBrowse = new ColumnConstraints(PolicyUiConstants.REMOVE_COLUMN_WIDTH);

        ColumnConstraints c1 = new ColumnConstraints(70);
        ColumnConstraints c2 = new ColumnConstraints(100);
        ColumnConstraints c3 = new ColumnConstraints(70);
        ColumnConstraints c4 = new ColumnConstraints(80);
        ColumnConstraints c5 = new ColumnConstraints(70);
        ColumnConstraints c6 = new ColumnConstraints(PolicyUiConstants.REMOVE_COLUMN_WIDTH);

        return List.of(c0, cBrowse, c1, c2, c3, c4, c5, c6);
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

        header.add(UiSupport.boldLabel("PathAndBelow"), 0, 0);
        header.add(new Label(""), 1, 0);
        header.add(UiSupport.boldLabel("Read"), 2, 0);
        header.add(UiSupport.boldLabel("Overwrite"), 3, 0);
        header.add(UiSupport.boldLabel("Create"), 4, 0);
        header.add(UiSupport.boldLabel("Execute"), 5, 0);
        header.add(UiSupport.boldLabel("Delete"), 6, 0);
        header.add(new Label(""), 7, 0);

        return header;
    }
}
