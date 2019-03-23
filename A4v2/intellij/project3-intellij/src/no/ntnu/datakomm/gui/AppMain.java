package no.ntnu.datakomm.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class AppMain extends Application {
    /**
     * This method is called automatically by JavaFX when the application is
     * launched
     *
     * @param primaryStage The main "stage" where the GUI will be rendered
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));
        Scene scene = new Scene(root, 600, 400);
        URL stylesheetUrl = getClass().getResource("style.css");
        scene.getStylesheets().add(stylesheetUrl.toString());
        primaryStage.setTitle("NTNU ÅLESUND - ID203012 - ChatClient");
        primaryStage.setScene(scene);
        URL imgUrl = getClass().getResource("ntnu.png");
        Image anotherIcon = new Image(imgUrl.toString());
        primaryStage.getIcons().add(anotherIcon);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
