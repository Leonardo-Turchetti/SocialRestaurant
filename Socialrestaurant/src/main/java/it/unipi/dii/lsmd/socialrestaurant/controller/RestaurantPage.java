package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jManager;
import it.unipi.dii.lsmd.socialrestaurant.model.*;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.ResourceBundle;

public class RestaurantPage implements Initializable {
    private Restaurant restaurant;
    private User user;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;
    private final int maxLength = 280;


    @FXML
    private Button backIcon;
    @FXML
    private Text name;

    @FXML
    private Text city;
    @FXML
    private Text cuisine;
    @FXML
    private Text likes;
    @FXML
    private VBox reviewsBox;
    @FXML
    private Button RestaurantListbtn;
    @FXML
    private Button review;
    @FXML
    private TextField commentText;
    @FXML
    private Text comNum;
    @FXML
    private Button likebtn;
    @FXML
    private Label boxLabel;
    @FXML
    private Button BookingBtn;
    @FXML
    private Button bookingPage;

    @FXML
    private Button reserveTable;
    @FXML
    private Button deleteRestaurant;
    @FXML
    private DatePicker dateBooking;

    @FXML
    private HBox bookingDate;

    @FXML
    private Label errorTf;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reviewsBox.setSpacing(10);
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        RestaurantListbtn.setOnMouseClicked(mouseEvent -> clickToRestaurantListBtn(mouseEvent));
        likebtn.setOnMouseClicked(mouseEvent -> clickLike(mouseEvent));
        BookingBtn.setOnMouseClicked(mouseEvent -> bookTable(mouseEvent));
        bookingPage.setOnMouseClicked(mouseEvent -> goToBookingPage(mouseEvent));
        reserveTable.setOnMouseClicked(mouseEvent -> clickOnReserveTable(mouseEvent));
        deleteRestaurant.setOnMouseClicked(mouseEvent -> clickOnDeleteRestaurant(mouseEvent));
        review.setOnMouseClicked(mouseEvent -> clickOnAddReviewBtn(mouseEvent));
    }



    public void setRestaurantPage (Restaurant restaurant) {
        //this.paper = mongoMan.getPaperById(p);
        this.restaurant = restaurant;
        this.user = Session.getInstance().getLoggedUser();

        // Push
        Session.getInstance().getPreviousPageRestaurant().add(restaurant);

        name.setText(restaurant.getName());
        cuisine.setText(restaurant.getCuisine());
        city.setText(restaurant.getCity());

        if (Objects.equals(user.getUsername(), "admin")) {
            bookingPage.setVisible(true);
            deleteRestaurant.setVisible(true);
        }else {
            bookingPage.setVisible(false);
            deleteRestaurant.setVisible(false);
        }


        if(neoMan.userAddRestaurant(user.getUsername(), restaurant))
            RestaurantListbtn.setText("Remove from Restaurant List");
        else
            RestaurantListbtn.setText("Add to Restaurant List");

        if(neoMan.userLikeRestaurant(user.getUsername(), restaurant))
            likebtn.setText("Unlike");
        else
            likebtn.setText("Like");
        likes.setText(Integer.toString(neoMan.getNumLikes(restaurant)));
        errorTf.setVisible(false);
        bookingDate.setVisible(false);
        reserveTable.setVisible(false);
        setReviewBox();
    }

    private void setReviewBox() {
        boxLabel.setText("Review List");
        int numReview = 0;
        reviewsBox.getChildren().clear();
        if (!restaurant.getReviewList().isEmpty()) {
            Iterator<Review> it = restaurant.getReviewList().iterator();

            while(it.hasNext()) {
                //HBox row = new HBox();
                //row.setAlignment(Pos.CENTER);
                //row.setStyle("-fx-padding: 10px");
                Review r = it.next();
                Pane p = loadReviewCard(r, restaurant);
                //row.getChildren().addAll(p);
                reviewsBox.getChildren().add(p);
                numReview++;
            }
        } else {
            reviewsBox.getChildren().add(new Label("No Review List :("));
        }
        comNum.setText(String.valueOf(numReview));
    }

    private Pane loadReviewCard (Review review, Restaurant restaurant) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/reviewcard.fxml"));
            pane = loader.load();
            ReviewCtrl ctrl = loader.getController();
            ctrl.textProperty().bindBidirectional(comNum.textProperty());
            ctrl.setReviewCard(review, restaurant, false);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPageRestaurant().remove(
                Session.getInstance().getPreviousPageRestaurant().size() - 1);

        // Check if previous page is Profile Page
        if (Session.getInstance().getPreviousPageUsers().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/browser.fxml", mouseEvent);
        else {
            ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", mouseEvent);

            ctrl.setProfilePage(Session.getInstance().getPreviousPageUsers().remove(
                    Session.getInstance().getPreviousPageUsers().size() - 1));
        }
    }

    private void clickToRestaurantListBtn (MouseEvent mouseEvent) {

        if(Objects.equals(RestaurantListbtn.getText(), "Add to Restaurant List")){
            neoMan.add(user, restaurant);
            RestaurantListObject r = new RestaurantListObject(restaurant.getName(), restaurant.getCity(), restaurant.getCuisine(), new Date());
            mongoMan.addRestaurantToRestaurantList(Session.getInstance().getLoggedUser().getUsername(), r);
            RestaurantListbtn.setText("Remove from Restaurant List");
        }else{
            neoMan.remove(user, restaurant);
            mongoMan.removeRestaurantFromRestaurantList(user,restaurant);
            RestaurantListbtn.setText("Add to Restaurant List");
        }

    }


    private void clickOnAddReviewBtn (MouseEvent mouseEvent){
        if((!commentText.getText().isEmpty()) && (commentText.getText().length() <= maxLength)){
            Review review = new Review(user.getUsername(), commentText.getText(), new Date());
            mongoMan.addReview(restaurant, review);
            restaurant = mongoMan.getRestaurantByName(restaurant.getName());
            setReviewBox();
            commentText.setText("");

        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Enter a review of up to 280 characters!");
            alert.showAndWait();
        }
    }

    private void clickLike (MouseEvent mouseEvent){
        if(Objects.equals(likebtn.getText(), "Like")){
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            neoMan.like(user, restaurant, timestamp);
            likes.setText(Integer.toString(neoMan.getNumLikes(restaurant)));
            likebtn.setText("UnLike");
        }else{
            neoMan.unlike(user, restaurant);
            likes.setText(Integer.toString(neoMan.getNumLikes(restaurant)));
            likebtn.setText("Like");
        }
    }

    private void bookTable (MouseEvent mouseEvent){
        bookingDate.setVisible(true);
        reserveTable.setVisible(true);
    }

    private void clickOnReserveTable(MouseEvent mouseEvent) {
        errorTf.setText("");
        if (dateBooking.getValue() != null && dateBooking.getValue().isBefore(LocalDate.now())) {
            errorTf.setVisible(true);
            errorTf.setText("The date of the booking have to be after the date of today.");
            return;
        }

        String timestamp = "";

        if (dateBooking.getValue() != null)
            timestamp = dateBooking.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if(mongoMan.checkBooking(user.getUsername(),restaurant.getName(),timestamp)==null) {
            Booking booking = new Booking(user.getUsername(), restaurant.getName(), restaurant.getCity(), restaurant.getCuisine(), timestamp);
            mongoMan.addBooking(booking);
        }else {
            errorTf.setVisible(true);
            errorTf.setText("You can not book a table with the same parameters");
        }
    }

    private void goToBookingPage(MouseEvent mouseEvent) {
        BookingPage ctrl = (BookingPage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/bookingpage.fxml", mouseEvent);

        ctrl.setBookingPageRestaurant(restaurant);
    }

    private void clickOnDeleteRestaurant(MouseEvent mouseEvent) {
        mongoMan.deleteRestaurant(restaurant);
        neoMan.deleteRestaurant(restaurant);
    }

}
