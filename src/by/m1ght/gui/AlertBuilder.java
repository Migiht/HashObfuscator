package by.m1ght.gui;

import javafx.scene.control.Alert;

final class AlertBuilder {
    private Alert.AlertType type;
    private String title;
    private String header;
    private String content;

    AlertBuilder setAlertType(Alert.AlertType type) {
        this.type = type;
        return this;
    }

    AlertBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    AlertBuilder setHeader(String header) {
        this.header = header;
        return this;
    }

    AlertBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    Alert build() {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
}
