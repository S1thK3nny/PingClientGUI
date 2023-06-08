module com.sith.pingclientgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.net.http;

    opens com.sith.pingclientgui to javafx.fxml, com.google.gson;
    exports com.sith.pingclientgui;
}