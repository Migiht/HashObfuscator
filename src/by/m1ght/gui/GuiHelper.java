package by.m1ght.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public final class GuiHelper {
    private static final Image icon = new Image(GuiObfuscator.class.getResourceAsStream("fx/icon.png"));
    private static final GaussianBlur blur = new GaussianBlur();

    public static Stage newStage(String name, Parent parent) {
        Stage stage = new Stage();
        stage.setTitle(name);
        stage.getIcons().add(icon);
        stage.setScene(new Scene(parent));
        stage.setResizable(false);
        return stage;
    }

    public static Image getIcon() {
        return icon;
    }

    public static Parent loadFXML(String name) throws IOException {
        return FXMLLoader.load(GuiObfuscator.class.getResource(name));
    }

    public static void enableBlur(Parent component) {
        component.setEffect(blur);
    }

    public static void disableBlur(Parent component) {
        component.setEffect(null);
    }
}
