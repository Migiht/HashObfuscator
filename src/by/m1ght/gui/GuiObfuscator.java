package by.m1ght.gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class GuiObfuscator extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = GuiHelper.loadFXML("fx/ui.fxml");
        stage.getIcons().add(GuiHelper.getIcon());
        stage.setResizable(false);
        stage.setTitle("Java Obfuscator");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
