package no.ntnu.datakomm.gui;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Class representing the main JavaFX application.
 *
 * For the assignment you probably don't need to modify anything in this file.
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method is called automatically by JavaFX when the application is
     * launched
     *
     * @param primaryStage The main "stage" where the GUI will be rendered
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL r = getClass().getClassLoader().getResource("layout.fxml");
        Parent root = FXMLLoader.load(r);
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add("styles/style.css");
        primaryStage.setTitle("NTNU ÅLESUND - ID203012 - ChatClient");
        primaryStage.setScene(scene);
        Image anotherIcon = new Image("styles/ntnu.png");
        primaryStage.getIcons().add(anotherIcon);
        primaryStage.show();
    }
}
