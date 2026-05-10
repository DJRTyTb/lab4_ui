package org.example.dice3d;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800, true);

        stage.setTitle("3D Dice");
        stage.setScene(scene);
        stage.show();
    }
}