package sample;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));

        Rectangle2D screenBound = Screen.getPrimary().getVisualBounds();

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Visual IDE");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.show();

        Controller controller = fxmlLoader.getController();

        primaryStage.setOnCloseRequest(windowEvent -> {
            if(controller.exit()){}
            else windowEvent.consume();//cancel event
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
