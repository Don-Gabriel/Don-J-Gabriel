package com.example.todoappv2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class ToDoApp extends Application {

    private TrayIcon trayIcon;

    @Override
    public void start(Stage stage) {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/todoappv2/todo-view.fxml");
            if (fxmlLocation == null) {
                System.err.println("ERROR: todo-view.fxml not found!");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);

            stage.setTitle("Enhanced To-Do App");
            stage.setScene(scene);

            // Handle close request
            stage.setOnCloseRequest((WindowEvent t) -> {
                t.consume(); // prevent default close
                minimizeToTray(stage);
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void minimizeToTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported, exiting...");
            Platform.exit();
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // Use your icon
        trayIcon = new TrayIcon(image, "To-Do App");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("To-Do App running");

        trayIcon.addActionListener(e -> Platform.runLater(stage::show));

        try {
            tray.add(trayIcon);
            stage.hide();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
