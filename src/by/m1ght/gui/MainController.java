package by.m1ght.gui;

import by.m1ght.Obfuscator;
import by.m1ght.config.Config;
import by.m1ght.util.Util;
import by.m1ght.util.IOUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.transformer.TransformerType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MainController {
    @FXML
    public Pane root;

    @FXML
    public TextField inputPath;

    @FXML
    public TextField outputPath;

    @FXML
    public TextField configPath;

    @FXML
    public ListView<String> libs;

    @FXML
    public Slider cores;

    private List<String> exclusions;

    public void initialize() {
        // Initialize
        exclusions = new ArrayList<>();
        cores.setMax(Runtime.getRuntime().availableProcessors());
        cores.setMin(1);

        loadDefaultJreLibs();
        // Select root pane
        root.requestFocus();
    }

    private void loadDefaultJreLibs() {
        if (System.getProperty("java.home") != null) {

            Path home = Paths.get(System.getProperty("java.home")).resolve("lib");

            Path rt = home.resolve("rt.jar");
            if (Files.exists(rt)) {
                libs.getItems().add(rt.toString());
            }

            Path jce = home.resolve("jce.jar");
            if (Files.exists(jce)) {
                libs.getItems().add(jce.toString());
            }
        }
    }

    public void handleRootClick() {
        root.requestFocus();
        libs.getSelectionModel().clearSelection();
    }

    public void handleLoadConfig() {
        try {
            fromConfig(Util.fromJson(Paths.get(configPath.getText()), null));
        } catch (Throwable e) {
            new AlertBuilder()
                    .setAlertType(Alert.AlertType.ERROR)
                    .setTitle("Ошибка")
                    .setHeader("Не удалось загрузить файл конфигурации")
                    .setContent(e.toString())
                    .build()
                    .showAndWait();
        }
    }

    private Config toConfig() {
        Config config = new Config();
        config.inputPath = inputPath.getText();
        config.outputPath = outputPath.getText();

        config.transformerConfigMap.get(TransformerType.METHOD_RENAME).enabled = ((CheckBox)root.lookup("#renameMethods")).isSelected();
        config.transformerConfigMap.get(TransformerType.METHOD_PUBLIC).enabled = ((CheckBox)root.lookup("#modifierMethods")).isSelected();
        config.transformerConfigMap.get(TransformerType.METHOD_SHUFFLE).enabled = ((CheckBox)root.lookup("#shuffleMethods")).isSelected();
        config.transformerConfigMap.get(TransformerType.METHOD_STATIC_MOVE).enabled = ((CheckBox)root.lookup("#moveMethods")).isSelected();

        config.transformerConfigMap.get(TransformerType.CLASS_RENAME).enabled = ((CheckBox)root.lookup("#renameClasses")).isSelected();
        config.transformerConfigMap.get(TransformerType.CLASS_PUBLIC).enabled = ((CheckBox)root.lookup("#modifierClasses")).isSelected();

        config.transformerConfigMap.get(TransformerType.FIELD_RENAME).enabled = ((CheckBox)root.lookup("#renameFields")).isSelected();
        config.transformerConfigMap.get(TransformerType.FIELD_PUBLIC).enabled = ((CheckBox)root.lookup("#modifierFields")).isSelected();
        config.transformerConfigMap.get(TransformerType.FIELD_SHUFFLE).enabled = ((CheckBox)root.lookup("#shuffleFields")).isSelected();

        config.transformerConfigMap.get(TransformerType.LOCAL_RENAME).enabled = ((CheckBox)root.lookup("#shuffleLocals")).isSelected();

        config.transformerConfigMap.get(TransformerType.DEBUG_CLEAR).enabled = ((CheckBox)root.lookup("#clearInfo")).isSelected();

        config.crashCRC = ((CheckBox)root.lookup("#crashCRC")).isSelected();

        return config;
    }

    private void fromConfig(Config config) {
        inputPath.setText(config.inputPath);
        inputPath.selectAll();
        outputPath.setText(config.outputPath);
        outputPath.selectAll();

        ((CheckBox)root.lookup("#renameMethods")).setSelected(config.transformerConfigMap.get(TransformerType.METHOD_RENAME).enabled);
        ((CheckBox)root.lookup("#modifierMethods")).setSelected(config.transformerConfigMap.get(TransformerType.METHOD_PUBLIC).enabled);
        ((CheckBox)root.lookup("#shuffleMethods")).setSelected(config.transformerConfigMap.get(TransformerType.METHOD_SHUFFLE).enabled);
        ((CheckBox)root.lookup("#moveMethods")).setSelected(config.transformerConfigMap.get(TransformerType.METHOD_STATIC_MOVE).enabled);

        ((CheckBox)root.lookup("#renameClasses")).setSelected(config.transformerConfigMap.get(TransformerType.CLASS_RENAME).enabled);
        ((CheckBox)root.lookup("#modifierClasses")).setSelected(config.transformerConfigMap.get(TransformerType.CLASS_PUBLIC).enabled);

        ((CheckBox)root.lookup("#renameFields")).setSelected(config.transformerConfigMap.get(TransformerType.FIELD_RENAME).enabled);
        ((CheckBox)root.lookup("#modifierFields")).setSelected(config.transformerConfigMap.get(TransformerType.FIELD_PUBLIC).enabled);
        ((CheckBox)root.lookup("#shuffleFields")).setSelected(config.transformerConfigMap.get(TransformerType.FIELD_SHUFFLE).enabled);

        ((CheckBox)root.lookup("#shuffleLocals")).setSelected(config.transformerConfigMap.get(TransformerType.LOCAL_RENAME).enabled);

        ((CheckBox)root.lookup("#clearInfo")).setSelected(config.transformerConfigMap.get(TransformerType.DEBUG_CLEAR).enabled);

        ((CheckBox)root.lookup("#crashCRC")).setSelected(config.crashCRC);

        exclusions.clear();
    }

    public void handleSaveConfig() {
        try {
            Util.writeJson(Paths.get(configPath.getText()), toConfig());
        } catch (Throwable e) {
            new AlertBuilder()
                    .setAlertType(Alert.AlertType.ERROR)
                    .setTitle("Ошибка")
                    .setHeader("Не удалось сохранить файл конфигурации")
                    .setContent(e.toString())
                    .build()
                    .showAndWait();
        }
    }

    // Load files
    public void handleSourceSelection() {
        FileChooser chooser = IOUtil.newFileChooser("Выберите исходный файл");
        File source = chooser.showOpenDialog(root.getScene().getWindow());
        if (source != null) {
            inputPath.setText(source.getAbsolutePath());
            inputPath.selectAll();
            outputPath.setText(source.getAbsolutePath() + "-obf");
            outputPath.selectAll();
        }
    }

    public void handleOutputSelection() {
        FileChooser chooser = IOUtil.newFileChooser("Выберите итоговоый файл");
        File output = chooser.showOpenDialog(root.getScene().getWindow());
        if (output != null) {
            outputPath.setText(output.getAbsolutePath());
            outputPath.selectAll();
        }
    }

    public void handleConfigSelection() {
        FileChooser chooser = IOUtil.newFileChooser("Выберите конфигурационный файл");
        File config = chooser.showOpenDialog(root.getScene().getWindow());
        if (config != null) {
            configPath.setText(config.getAbsolutePath());
            configPath.selectAll();
        }
    }

    public void handleLibFileAdd() {
        FileChooser chooser = IOUtil.newFileChooser("Выберите библиотеки");
        List<File> source = chooser.showOpenMultipleDialog(root.getScene().getWindow());
        if (source != null) {
            libs.getItems().addAll(source.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        }
    }

    public void handleLibFolderAdd() {
        DirectoryChooser chooser = IOUtil.newDirChooser("Выберите библиотеки");
        File source = chooser.showDialog(root.getScene().getWindow());
        if (source != null) {
            libs.getItems().add(source.getAbsolutePath());
        }
    }

    public void handleLibRemove() {
        libs.getItems().removeAll(libs.getSelectionModel().getSelectedItems());
    }

    public void handleExclusions() {
        // New window (Stage)
        GuiHelper.enableBlur(root);
        try {
            Parent excls = GuiHelper.loadFXML("fx/exclusions.fxml");
            ObservableList exclList = ((ListView) excls.lookup("#list")).getItems();
            exclList.setAll(exclusions.toArray());
            Stage stage = GuiHelper.newStage("Исключения", excls);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.showAndWait();
            exclusions = new ArrayList<>(exclList);

            System.out.println(exclusions);
            System.out.println(exclusions.size());
        } catch (Throwable t) {
            new AlertBuilder()
                    .setAlertType(Alert.AlertType.ERROR)
                    .setTitle("Ошибка")
                    .setHeader("Ошибка при открытии окна с исключениями")
                    .setContent(t.toString())
                    .build()
                    .showAndWait();
        }
        GuiHelper.disableBlur(root);
    }

    public void handleLibClear() {
        libs.getItems().clear();
        loadDefaultJreLibs();
    }


    public void handleStart() {
        GuiHelper.enableBlur(root);
        handleSaveConfig();
        try {
            Parent debug = GuiHelper.loadFXML("fx/debug.fxml");
            Stage stage = GuiHelper.newStage("Консоль", debug);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(root.getScene().getWindow());

            LogUtil.addOutput(message ->
                    Platform.runLater(() ->
                            ((ListView) debug.lookup("#output")).getItems().add(message)));

            // Starting obfuscator
            CompletableFuture.runAsync(()->
                    new Obfuscator(toConfig()));
            stage.showAndWait();
        } catch (Throwable t) {
            new AlertBuilder()
                    .setAlertType(Alert.AlertType.ERROR)
                    .setTitle("Ошибка")
                    .setHeader("Обфускация завершилась неудачно")
                    .setContent(t.toString())
                    .build()
                    .showAndWait();
        }
        GuiHelper.disableBlur(root);
    }
}
