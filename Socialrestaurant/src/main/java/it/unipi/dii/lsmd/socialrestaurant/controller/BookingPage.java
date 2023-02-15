package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.model.*;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Iterator;
import java.util.List;

public class BookingPage {

    private User user;
    private Restaurant restaurant;
    private MongoDBManager mongoMan;

    @FXML
    private Button backIcon;

    @FXML
    private Label boxLabel;

    @FXML
    private VBox bookingBox;

    public void initialize() {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));

    }
    public void setBookingPageUser(User user) {
        this.user = user;

        // Push
        Session.getInstance().getPreviousPageUsers().add(user);

        setReviewBoxUser();
    }

    public void setBookingPageRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        // Push
        Session.getInstance().getPreviousPageRestaurant().add(restaurant);

        setReviewBoxRestaurant();
    }

    private void setReviewBoxUser() {
        boxLabel.setText("Booking List");
        bookingBox.getChildren().clear();
        List<Booking> bookingList;
        bookingList = mongoMan.getBookingListByUser(user.getUsername());
        if (!bookingList.isEmpty()) {
            Iterator<Booking> it = bookingList.iterator();

            while(it.hasNext()) {
                //HBox row = new HBox();
                //row.setAlignment(Pos.CENTER);
                //row.setStyle("-fx-padding: 10px");
                Booking b = it.next();
                Pane p = loadBookingCard(b);
                //row.getChildren().addAll(p);
                bookingBox.getChildren().add(p);

            }
        } else {
            bookingBox.getChildren().add(new Label("No Booking List :("));
        }
    }

    private void setReviewBoxRestaurant() {
        boxLabel.setText("Booking List");
        bookingBox.getChildren().clear();
        List<Booking> bookingList;
        bookingList = mongoMan.getBookingListByRestaurant(restaurant.getName());
        if (!bookingList.isEmpty()) {
            Iterator<Booking> it = bookingList.iterator();

            while(it.hasNext()) {
                //HBox row = new HBox();
                //row.setAlignment(Pos.CENTER);
                //row.setStyle("-fx-padding: 10px");
                Booking b = it.next();
                Pane p = loadBookingCard(b);
                //row.getChildren().addAll(p);
                bookingBox.getChildren().add(p);

            }
        } else {
            bookingBox.getChildren().add(new Label("No Booking List :("));
        }
    }

    private Pane loadBookingCard (Booking booking) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/bookingcard.fxml"));
            pane = loader.load();
            BookingCtrl ctrl = loader.getController();
            ctrl.setBookingCard(booking, null, 0);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon(MouseEvent mouseEvent) {

        if(user!=null) {
            // Pop
            Session.getInstance().getPreviousPageUsers().remove(Session.getInstance().getPreviousPageUsers().size() - 1);
            ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", mouseEvent);
            ctrl.setProfilePage(Session.getInstance().getPreviousPageUsers().remove(
                    Session.getInstance().getPreviousPageUsers().size() - 1));
        }
        else {
            RestaurantPage ctrl = (RestaurantPage) Utils.changeScene(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantpage.fxml", mouseEvent);
            ctrl.setRestaurantPage(Session.getInstance().getPreviousPageRestaurant().remove(
                    Session.getInstance().getPreviousPageRestaurant().size() - 1));
        }
    }

}
