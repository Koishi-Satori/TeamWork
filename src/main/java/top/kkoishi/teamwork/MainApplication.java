package top.kkoishi.teamwork;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import top.kkoishi.proc.property.BuildFailedException;
import top.kkoishi.proc.xml.XmlDomParser;
import top.kkoishi.proc.xml.XmlSyntaxException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MainApplication extends Application {

    static FXMLLoader INIT_LOADER;

    public static Path path;

    static {
        try {
            INIT_LOADER = new FXMLLoader(Path.of("./data/main.fxml").toUri().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start (Stage stage) throws IOException {
        final Scene scene = new Scene(INIT_LOADER.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.setOnCloseRequest(we -> {

        });
        stage.show();
    }

    public static void main (String[] args) {
        if (args.length == 0) {
            path = null;
        } else {
            path = Path.of(args[0]);
        }
        launch(args);
    }
}