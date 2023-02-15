package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.model.Restaurant;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class RestaurantCtrl {
    private MongoDBManager mongoMan;
    private Restaurant restaurant;

    @FXML private Label restaurantName;
    @FXML private Text cuisine;
    @FXML private Text  city;

    @FXML private Label analyticLabelName;
    @FXML private Text analyticValue;


    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        restaurantName.setOnMouseClicked(mouseEvent -> clickOnRestaurantName(mouseEvent));
    }

    public void setRestaurantCard (Restaurant restaurant, String analyticLabelName, int analyticValue) {
        this.restaurant = restaurant;

        restaurantName.setText(restaurant.getName());
        city.setText(restaurant.getCity());
        cuisine.setText(restaurant.getCuisine());

        if (analyticLabelName != null) {
            this.analyticLabelName.setText(analyticLabelName);
            this.analyticValue.setText(String.valueOf(analyticValue));
        }
        else {
            this.analyticLabelName.setVisible(false);
            this.analyticValue.setVisible(false);
        }
    }

    private void clickOnRestaurantName (MouseEvent mouseEvent) {
        RestaurantPage ctrl = (RestaurantPage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantpage.fxml", mouseEvent);

        restaurant = mongoMan.getRestaurantByName(restaurant.getName());
        ctrl.setRestaurantPage(restaurant);
    }

}
