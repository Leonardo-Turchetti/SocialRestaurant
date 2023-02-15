package it.unipi.dii.lsmd.socialrestaurant;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            stage.setOnCloseRequest((WindowEvent we) -> {
                MongoDriver.getInstance().closeConnection();
                Neo4jDriver.getInstance().closeConnection();
                System.exit(0);
            });
            Parent root = FXMLLoader.load(getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/layout/login.fxml")); //login
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("SocialRestaurant");
            //stage.getIcons().add(new Image(getClass().getResourceAsStream( "img/iconApp.png")));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}