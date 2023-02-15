package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jManager;
import it.unipi.dii.lsmd.socialrestaurant.model.Restaurant;
import it.unipi.dii.lsmd.socialrestaurant.model.Review;
import it.unipi.dii.lsmd.socialrestaurant.model.Session;
import it.unipi.dii.lsmd.socialrestaurant.model.User;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;

public class ReviewCtrl {
    private Review review;
    private Restaurant restaurant;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;
    private StringProperty text = new SimpleStringProperty();

    @FXML private Label username;
    @FXML private Text timestamp;
    @FXML private Text comment;
    @FXML private ImageView bin;
    @FXML private ImageView modify;
    @FXML private AnchorPane reviewBox;
    @FXML private ScrollPane scrollpane;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        modify.setOnMouseClicked(mouseEvent -> clickOnModify(mouseEvent));
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        username.setOnMouseClicked(mouseEvent -> clickOnUsername(mouseEvent));
    }

    public void setReviewCard (Review review, Restaurant restaurant, boolean browser) {
        this.review = review;
        this.restaurant = restaurant;
        if (browser)
            bin.setOnMouseClicked(mouseEvent -> clickOnBinBrowser(mouseEvent));
        else
            bin.setOnMouseClicked(mouseEvent -> clickOnBin(mouseEvent));
        if(Objects.equals(Session.getInstance().getLoggedUser().getUsername(), review.getUsername())) {
            bin.setVisible(true);
            modify.setVisible(true);
        } else {
            if(Session.getInstance().getLoggedUser().getRole() > 0) //If the user is an admin can delete other comments
                bin.setVisible(true);
            else
                bin.setVisible(false);
            modify.setVisible(false);
        }
        if (review.getUsername().equals("Deleted user"))
            username.setDisable(true);
        username.setText(review.getUsername());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        timestamp.setText(formatter.format(review.getTimestamp()));
        comment.setText(review.getText());
    }

    public StringProperty textProperty() {
        return text ;
    }

    private final String getText() {
        return textProperty().get();
    }

    private final void setText(String text) {
        textProperty().set(text);
    }

    private void clickOnBin (MouseEvent mouseEvent) {
        mongoMan.deleteReview(restaurant, review);
        ((VBox) reviewBox.getParent()).getChildren().remove(reviewBox);
        int numComm = Integer.parseInt(getText());
        numComm--;
        setText(String.valueOf(numComm));
    }

    private void clickOnBinBrowser (MouseEvent mouseEvent) {
        restaurant = mongoMan.getRestaurantByName(restaurant.getName());
        mongoMan.deleteReview(restaurant, review);
        ((GridPane) reviewBox.getParent()).getChildren().remove(reviewBox);
    }

    private void clickOnModify (MouseEvent mouseEvent) {
        TextInputDialog dialog = new TextInputDialog(review.getText());
        dialog.setHeaderText(null);
        dialog.setTitle("Edit comment");
        Optional<String> result = dialog.showAndWait();
        review.setText(result.get());
        comment.setText(result.get());
        if (result.isPresent()){
            mongoMan.updateReview(restaurant, review);
        }
    }

    private void clickOnUsername(MouseEvent mouseEvent){
        if(!review.getUsername().equals("Deleted user")) {
            User u = mongoMan.getUserByUsername(review.getUsername());
            ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", mouseEvent);
            ctrl.setProfilePage(u);
        }
    }
}