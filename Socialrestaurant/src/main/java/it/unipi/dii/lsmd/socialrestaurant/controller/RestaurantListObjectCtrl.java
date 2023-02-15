package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jManager;
import it.unipi.dii.lsmd.socialrestaurant.model.Restaurant;
import it.unipi.dii.lsmd.socialrestaurant.model.RestaurantListObject;
import it.unipi.dii.lsmd.socialrestaurant.model.Session;
import it.unipi.dii.lsmd.socialrestaurant.model.User;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class RestaurantListObjectCtrl {
    private MongoDBManager mongoMan;

    private Neo4jManager neoMan;
    private RestaurantListObject object;
    private User user;

    @FXML private Label restaurantName;
    @FXML private Text timestamp;
    @FXML private Text city;
    @FXML private Text cuisine;

    @FXML private AnchorPane restaurantListObjectBox;
    @FXML
    private Button removeRestaurantBtn;


    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        restaurantName.setOnMouseClicked(mouseEvent -> clickOnRestaurantName(mouseEvent));
        removeRestaurantBtn.setOnMouseClicked(mouseEvent -> clickOnRemoveFromRestaurantList(mouseEvent));
    }

    public void setRestaurantListObjectCard (RestaurantListObject object, User user) {
        this.object = object;
        this.user = user;

        restaurantName.setText(object.getName());
        city.setText(object.getCity());
        cuisine.setText(object.getCuisine());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        timestamp.setText(formatter.format(object.getTimestamp()));
        if(Objects.equals(Session.getInstance().getLoggedUser().getUsername(), user.getUsername())) {
            removeRestaurantBtn.setVisible(true);
        }
        else{
            removeRestaurantBtn.setVisible(false);
        }
    }

    private void clickOnRestaurantName (MouseEvent mouseEvent) {
        Restaurant restaurant = mongoMan.getRestaurantByName(object.getName());

        RestaurantPage ctrl = (RestaurantPage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantpage.fxml", mouseEvent);

        ctrl.setRestaurantPage(restaurant);

    }

    private void clickOnRemoveFromRestaurantList(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remove Restaurant?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            // Get the restaurant list
            Restaurant restaurant = mongoMan.getRestaurantByName(object.getName());
            mongoMan.removeRestaurantFromRestaurantList(user,restaurant);
            neoMan.remove(user, restaurant);
            ((HBox) restaurantListObjectBox.getParent()).getChildren().remove(restaurantListObjectBox);
        }
    }
}
