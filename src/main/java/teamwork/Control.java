package teamwork;

import javafx.beans.value.ObservableValueBase;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import top.kkoishi.proc.json.JsonJavaBridge;
import top.kkoishi.proc.json.JsonParser;
import top.kkoishi.proc.json.JsonSyntaxException;
import top.kkoishi.proc.json.MappedJsonObject;
import top.kkoishi.proc.property.BuildFailedException;
import top.kkoishi.teamwork.MainApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author DELL
 */
public final class Control implements Initializable {
    public TextField textField;
    public Menu file;
    public MenuItem newItem;
    public MenuItem open;
    public Menu openRec;
    public MenuItem close;
    public MenuItem save;
    public MenuItem saveAs;
    public MenuItem revert;
    public TreeView<String> structure;
    public TreeItem<String> root;
    public TableColumn<Utils.HistoryData, String> history;
    public TableColumn<Utils.HistoryData, Path> location;
    public TableView<Utils.HistoryData> useTable;
    public ScrollPane workspace;
    public Text searchConfirm;
    public Button post;
    public Button next;
    public MenuItem delete;
    public MenuItem cut;
    public MenuItem copy;
    public MenuItem paste;
    private Path path = MainApplication.path;
    private final LinkedList<Path> oldStack = new LinkedList<>();
    private final LinkedList<Path> newStack = new LinkedList<>();

    static ArrayDeque<Path> buf = new ArrayDeque<>(8);
    static final Runtime R = Runtime.getRuntime();
    static JsonParser D;
    static MappedJsonObject M;
    static Utils.CompType type;
    static AtomicBoolean useAccuracy = new AtomicBoolean(true);
    static final Path ROOT_DIR = null;
    static final String[] OPTIONS = {"Name Only", "Content Include", "Text Match"};
    static int maxOldStack = 32;
    static int maxNewStack = 32;
    private static Utils.HistoryData[] recent;

    static {
        // init static field.
        try {
            // parse json and load class fields.
            reload();
        } catch (IOException | BuildFailedException | JsonSyntaxException | InvocationTargetException
                | NoSuchMethodException | InstantiationException | IllegalAccessException |
                NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void reload () throws IOException, JsonSyntaxException, BuildFailedException,
            NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchFieldException {
        D = new JsonParser(Files.readString(Path.of("./data/data.json")));
        D.parse();
        M = MappedJsonObject.cast(D.result(), HashMap.class);
        // lookup type and if use accuracy size.
        type = Utils.CompType.valueOf(M.getString("look_up"));
        useAccuracy.set(M.getBool("use_acc"));
        maxOldStack = M.getNumber("max_old_stack").intValue();
        maxNewStack = M.getNumber("max_new_stack").intValue();
        final var array = (Object[]) M.get("recent");
        recent = new Utils.HistoryData[array.length];
        for (int i = 0; i < recent.length; i++) {
            recent[i] = JsonJavaBridge.cast(Utils.HistoryData.class, (MappedJsonObject) array[i]);
        }
    }

    public Control () {
    }

    private void searchName (String pattern, Path root) {
        try {
            System.out.println("Search:" + root);
            final ArrayList<Path> result = new ArrayList<>(32);
            Files.walkFileTree(root, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.toString().contains(pattern)) {
                        result.add(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().contains(pattern)) {
                        result.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory (Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            final Path[] paths = new Path[result.size()];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = result.remove(0);
            }
            final ChoiceDialog<Path> choiceDialog = new ChoiceDialog<>(null, paths);
            choiceDialog.setTitle("Search Result");
            choiceDialog.setContentText("You can choose a result to open it.");
            final var option = choiceDialog.showAndWait();
            if (option.isEmpty()) {
                return;
            }
            if (Files.isDirectory(option.get())) {
                accessDir(option.get());
            } else {
                accessFile(option.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchExtension (String extension, Path root) {
        try {
            System.out.println("Search:" + root + "<-" + extension);
            final ArrayList<Path> result = new ArrayList<>(32);
            Files.walkFileTree(root, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(extension)) {
                        result.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory (Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            final Path[] paths = new Path[result.size()];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = result.remove(0);
            }
            final ChoiceDialog<Path> choiceDialog = new ChoiceDialog<>(null, paths);
            choiceDialog.setTitle("Search Result");
            choiceDialog.setContentText("You can choose a result to open it.");
            final var option = choiceDialog.showAndWait();
            if (option.isEmpty()) {
                return;
            }
            if (Files.isDirectory(option.get())) {
                accessDir(option.get());
            } else {
                accessFile(option.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete (Path path) {
        if (Files.isDirectory(path)) {
            try {
                Files.list(path).forEach(Control::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path pasteName (Path src) {
        throw new RuntimeException("");
    }

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        searchConfirm.setOnMouseClicked(me -> {
            final ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Search Types", OPTIONS);
            choiceDialog.setTitle("Choice Search Type");
            choiceDialog.setContentText("");
            final var option = choiceDialog.showAndWait();
            if (option.isEmpty()) {
                return;
            }
            if (path == null) {
                return;
            }
            final var item = option.get();
            if (OPTIONS[1].equals(item)) {

            } else if (OPTIONS[0].equals(item)) {
                final var name = textField.getText();
                if (name == null) {
                    return;
                }
                if (name.startsWith("*.")) {
                    searchExtension(name.substring(1), path);
                } else {
                    searchName(name, path);
                }
            } else if ("Text Match".equals(item)) {

            }
        });
        newItem.setOnAction(ae -> createAction());
        open.setOnAction(ae -> {
            final File file = chooseFile();
            if (!file.isDirectory()) {
                accessFile(file.toPath());
            }
        });
        copy.setOnAction(ae -> {
            final var select = ((Utils.CellContainer) workspace.getContent()).getSelect();
            if (select != null) {
                buf.clear();
                buf.addLast(select);
            }
        });
        paste.setOnAction(ae -> {
            if (!buf.isEmpty()) {
                while (!buf.isEmpty()) {
                    final var p = buf.removeLast();
                    try {
                        Files.copy(p, Path.of(System.currentTimeMillis() + "-" + p));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        delete.setOnAction(ae -> {
            final var select = ((Utils.CellContainer) workspace.getContent()).getSelect();
            if (select != null) {
                try {
                    delete(select);
                } catch (Exception e) {
                    e.printStackTrace();
                    final Alert dialog = new Alert(Alert.AlertType.INFORMATION);
                    dialog.setTitle(e.getClass().toString());
                    dialog.setContentText(e.getMessage());
                    dialog.setOnCloseRequest(de -> dialog.hide());
                    dialog.show();
                }
            } else {
                try {
                    delete(chooseFile().toPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    final Alert dialog = new Alert(Alert.AlertType.INFORMATION);
                    dialog.setTitle(e.getClass().toString());
                    dialog.setContentText(e.getMessage());
                    dialog.setOnCloseRequest(de -> dialog.hide());
                    dialog.show();
                }
            }
            final Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Success");
            dialog.setContentText("Success to delete the select one!");
            dialog.setOnCloseRequest(de -> dialog.hide());
            dialog.show();
            accessDirImpl(path);
        });
        initTable();
        final MenuItem iconLookup = new MenuItem("Icon Lookup");
        final MenuItem infoLookup = new MenuItem("Information Lookup");
        final MenuItem contentLookup = new MenuItem("Content Lookup");
        final ContextMenu contextMenu = new ContextMenu(
                iconLookup,
                infoLookup,
                contentLookup,
                new SeparatorMenuItem()
        );
        contextMenu.setPrefSize(100, 100);
        workspace.setOnMouseClicked(me -> {
            if (me.isPopupTrigger()) {
                // pop up.
                contextMenu.show(workspace, me.getScreenX(), me.getScreenY());
            }
        });
        accessDir(path);
        post.setOnMouseClicked(me -> this.lastLevel());
        next.setOnMouseClicked(me -> this.nextLevel());
        initTree();
    }

    private File chooseFile () {
        final FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("./"));
        fc.setTitle("Choose a file");
        final var file = fc.showOpenDialog(new Stage(StageStyle.UNDECORATED));
        return file;
    }

    private void createAction () {
        final ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(null, "Directory", "File");
        choiceDialog.setContentText("Choose the type to create.");
        choiceDialog.setTitle("Choice");
        final var option = choiceDialog.showAndWait();
        if (option.isEmpty()) {
            return;
        }
        final TextInputDialog textInputDialog = new TextInputDialog(null);
        textInputDialog.setContentText("Input file name");
        textInputDialog.setTitle("Create");
        final var name = textInputDialog.showAndWait();
        final var value = option.get();
        if (name.isEmpty()) {
            final Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setContentText("The file name can not be empty!");
            dialog.setOnCloseRequest(de -> dialog.hide());
            dialog.show();
            return;
        }
        final var p = Path.of(path + "/" + name.get());
        if ("Directory".equals(value)) {
            try {
                System.out.println(Files.createDirectory(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println(Files.createFile(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        accessDir(path);
    }

    private void initTable () {
        history.setCellValueFactory(df -> new ObservableValueBase<>() {
            @Override
            public String getValue () {
                return df.getValue().name();
            }
        });
        useTable.setOnMouseClicked(me -> {
            if (me.getClickCount() == 2) {
                final var ele = useTable.getSelectionModel().getSelectedItem();
                if (ele != null) {
                    accessFile(ele.location());
                }
            }
        });
        location.setCellValueFactory(df -> new ObservableValueBase<>() {
            @Override
            public Path getValue () {
                return df.getValue().location();
            }
        });
        final var array = (Object[]) M.get("recent");
        System.out.println(Arrays.toString(array));
        useTable.getItems().addAll();
    }

    private void initTree () {
        root = path == null ? TreeItemImpl.diskRoot : new TreeItemImpl(path);
        root.getChildren();
        structure.setRoot(root);
        structure.setOnMouseClicked(me -> {
            final var count = me.getClickCount();
            if (count == 1) {
                final var selected = structure.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }
                ((TreeItemImpl) selected).upgrade();
                selected.setExpanded(!selected.isExpanded());
                System.out.println("Expand:" + selected);
            } else if (count == 2) {
                final var selected = (TreeItemImpl) structure.getSelectionModel().getSelectedItem();
                if (selected == null || selected.path == null) {
                    return;
                }
                if (Files.isDirectory(selected.path)) {
                    accessDir(selected.path);
                } else {
                    accessFile(selected.path);
                }
            }
        });
    }

    public void switchWordspace () {
    }

    public void lastLevel () {
        System.out.println(oldStack);
        System.out.println(newStack);
        if (newStack.size() > maxNewStack) {
            newStack.removeLast();
        }
        if (oldStack.isEmpty()) {
            if (path == null) {
                accessDirImpl(null);
                return;
            }
            final var parent = path.getParent();
            accessDirImpl(parent);
            newStack.addLast(parent);
        } else {
            accessDirImpl(oldStack.removeLast());
            newStack.addLast(path);
        }
    }

    public void nextLevel () {
        System.out.println(oldStack);
        System.out.println(newStack);
        if (oldStack.size() > maxOldStack) {
            oldStack.removeLast();
        }
        if (newStack.isEmpty()) {
            if (path == null) {
                accessDirImpl(null);
                return;
            }
            final var parent = path.getRoot();
            System.out.println("root:" + parent);
            accessDirImpl(parent);
            oldStack.addLast(parent);
        } else {
            accessDirImpl(newStack.removeLast());
            oldStack.addLast(path);
        }
    }

    public void accessDir (Path path) {
        oldStack.add(path);
        accessDirImpl(path);
    }

    private void accessDirImpl (Path path) {
        this.path = path;
        if (path == ROOT_DIR) {
            final var content = Utils.FS.getRootDirectories();
            int count = 0;
            for (final Path ignored : content) {
                count++;
            }
            final var buffer = new Path[count];
            count = 0;
            for (final Path value : content) {
                buffer[count++] = value;
            }
            try {
                final var pane = new Utils.CellContainer(type, useAccuracy.get(), buffer);
                pane.setAction(this::autoAccess);
                workspace.setContent(pane);
            } catch (IOException e) {
                e.printStackTrace();
                // this should not happen.
            }
        } else {
            try {
                final var content = Files.list(path).toArray(Path[]::new);
                final var pane = new Utils.CellContainer(type, useAccuracy.get(), content);
                pane.setAction(this::autoAccess);
                workspace.setContent(pane);
            } catch (IOException e) {
                e.printStackTrace();
                // this should not happen.
            }
        }
    }

    private void autoAccess (Path path) {
        if (Files.isDirectory(path)) {
            accessDir(path);
        } else {
            accessFile(path);
        }
    }

    public void accessFile (Path path) {
        final var o = new Utils.HistoryData(path.getFileName().toString(), path);
        if (!useTable.getItems().contains(o)) {
            useTable.getItems().add(o);
            recent = Arrays.copyOf(recent, recent.length + 1);
            recent[recent.length - 1] = o;
        }
        try {
            R.exec(new String[]{"powershell", "start", "\"" + path.toRealPath() + "\""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Utils.HistoryData[] getRecent () {
        return recent;
    }

    public static void writeProc () {

    }
}
