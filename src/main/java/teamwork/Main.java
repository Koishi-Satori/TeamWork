package teamwork;

import javafx.application.Application;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main extends Application {
    static Path path;

    final VBox parent = new VBox();
    final AnchorPane anchorPane = new AnchorPane();
    final TextField searchTextField = new TextField();
    final Text searchText = new Text("AllUWant");

    final MenuBar menuBar = new MenuBar();
    final Menu file = new Menu("File");
    final MenuItem newItem = new MenuItem("New");
    final MenuItem open = new MenuItem("Open");
    final MenuItem openRec = new MenuItem("Open Recent");


    final TreeView<Path> structure = new TreeView<>(new TreeItem<>(path == null ? Path.of("C://") : path));

    public Main () {
    }

    private void iinit () {
        searchTextField.setPromptText("\t\tSearch in this computer.");
        searchText.setOnMouseClicked(me -> System.out.println(searchTextField.getText()));
        structure.setOnEditStart(ee -> initTree(ee.getTreeItem()));
        final ScrollPane left = new ScrollPane(structure);
        anchorPane.getChildren().add(left);
        parent.setPrefSize(500, 500);
        VBox.setVgrow(anchorPane, Priority.ALWAYS);
        menuBar.setPrefSize(500, 38);
        left.setPrefSize(200, 500);
        initTree(structure.getRoot());
        System.out.println(structure.getRoot().getValue().toAbsolutePath());
        structure.setOnMouseClicked(me -> {

        });
    }

    private void initTree (TreeItem<Path> root) {
        final var value = root.getValue();
        if (Files.isDirectory(value)) {
            try {
                Files.list(value).map(TreeItem::new).peek(System.out::println).forEach(root.getChildren()::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String[] args) {
        if (args.length == 0) {
            path = null;
        } else {
            path = Path.of(args[0]);
        }
        launch();
    }

    @Override
    public void start (Stage stage) throws Exception {
        final Scene s = new Scene(parent, 500, 500);
        iinit();
        stage.setScene(s);
        stage.show();
    }
}
