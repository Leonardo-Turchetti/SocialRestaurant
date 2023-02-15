package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.model.Booking;
import it.unipi.dii.lsmd.socialrestaurant.model.Restaurant;
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

public class BookingCtrl {
    private MongoDBManager mongoMan;

    private Booking object;

    @FXML private Label restaurantName;
    @FXML private Label userName;
    @FXML private Text timestamp;
    @FXML private Text city;
    @FXML private Text cuisine;

    @FXML private AnchorPane bookingBox;
    @FXML
    private Button cancelBookingBtn;

    @FXML private Label analyticLabelName;
    @FXML private Text analyticValue;


    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        restaurantName.setOnMouseClicked(mouseEvent -> clickOnRestaurantName(mouseEvent));
        userName.setOnMouseClicked(mouseEvent -> clickOnUserName(mouseEvent));
        cancelBookingBtn.setOnMouseClicked(mouseEvent -> clickOnCancelBooking(mouseEvent));
    }

    public void setBookingCard (Booking object, String analyticLabelName, int analyticValue) {
        this.object = object;


        restaurantName.setText(object.getRestaurantname());
        userName.setText(object.getUsername());
        city.setText(object.getCity());
        cuisine.setText(object.getCuisine());
        timestamp.setText(object.getTimestamp());

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
        Restaurant restaurant = mongoMan.getRestaurantByName(object.getRestaurantname());

        RestaurantPage ctrl = (RestaurantPage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantpage.fxml", mouseEvent);

        ctrl.setRestaurantPage(restaurant);

    }

    private void clickOnUserName (MouseEvent mouseEvent) {
        User user = mongoMan.getUserByUsername(object.getUsername());

        ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", mouseEvent);

        ctrl.setProfilePage(user);

    }

    private void clickOnCancelBooking(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Cancel Booking?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            mongoMan.removeBooking(object);
            ((HBox) bookingBox.getParent()).getChildren().remove(bookingBox);
        }
    }
}

