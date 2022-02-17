package by.m1ght.gui;

import by.m1ght.transformer.TransformerType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public final class ExclusionController {
    @FXML
    public ComboBox<TransformerType> comboBox;

    @FXML
    public TextField exclusionName;

    @FXML
    public ListView list;

    public void initialize() {
        comboBox.getItems().addAll(TransformerType.values());
    }

    public void handleExclusionsClear() {
        list.getItems().clear();
    }

    public void handleExclusionsRemove() {
        list.getItems().removeAll(list.getSelectionModel().getSelectedItems());
    }

    public void handleExclusionsAdd() {
        if (!comboBox.getSelectionModel().isEmpty() && exclusionName.getText().length() > 0) {
            list.getItems().add(comboBox.getSelectionModel().getSelectedItem().toString() + ":" + exclusionName.getText());
        }
    }
}
