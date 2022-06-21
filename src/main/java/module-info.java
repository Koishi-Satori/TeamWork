module top.kkoishi.teamwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires kotlin.stdlib;
    requires Properties;
    requires java.desktop;

    opens top.kkoishi.teamwork to javafx.fxml;
    opens teamwork to javafx.graphics, javafx.fxml;
    exports top.kkoishi.teamwork;
}