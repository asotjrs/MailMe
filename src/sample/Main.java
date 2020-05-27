package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.datamodel.ContactData;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Mail Me");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

    }

    @Override
    public void init() throws Exception {
        super.init();
        if (!ContactData.getInstance().open()) {
            System.out.println("Fatal Error: couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        ContactData.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}