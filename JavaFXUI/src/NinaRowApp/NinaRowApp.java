package NinaRowApp;

import NinaRowApp.components.ninaRowApp.NinaRowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class NinaRowApp extends Application {

    public static void main(String[] args) {
        launch(NinaRowApp.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/NinaRowApp/components/ninaRowApp/NinaRowApp.fxml");
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        primaryStage.setTitle("N in a Row");
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        NinaRowController ninaRowController =  fxmlLoader.getController();
        ninaRowController.setScene(scene);
        primaryStage.show();
    }
}
