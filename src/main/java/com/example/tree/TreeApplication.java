package com.example.tree;

import com.example.tree.controller.ControllerLoader;
import com.example.tree.controller.MainController;
import com.example.tree.exception.LoadingException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TreeApplication extends Application {

    private static final ControllerLoader CONTROLLER_LOADER = new ControllerLoader(TreeApplication.class);

    public static ControllerLoader getControllerLoader() {
        return CONTROLLER_LOADER;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws LoadingException {
        MainController controller = MainController.load(getControllerLoader());
        Scene scene = new Scene(controller.getRoot());
        stage.setTitle("Tree viewer");
        stage.setScene(scene);
        stage.show();
    }
}