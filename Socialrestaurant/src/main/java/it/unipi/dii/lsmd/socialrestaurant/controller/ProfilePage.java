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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class ProfilePage {
    private User user;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;

    @FXML
    private Button backIcon;
    @FXML
    private ImageView editIcon;
    @FXML
    private ImageView profileImg;
    @FXML
    private Label username;
    @FXML
    private Text email;
    @FXML
    private Text name;
    @FXML
    private Text surname;
    @FXML
    private Text age;
    @FXML
    private Text nFollower;
    @FXML
    private Text nFollowing;
    @FXML
    private Text nRestaurantAdded;
    @FXML
    private TextField nameRestaurant;
    @FXML
    private TextField cityRestaurant;
    @FXML
    private TextField cuisineRestaurant;
    @FXML
    private Button followBtn;
    @FXML
    private Label boxLabel;
    @FXML
    private VBox box;
    @FXML
    private VBox addRestaurantBox;
    @FXML
    private Button deleteUserBtn;
    @FXML
    private Button bookingBtn;
    @FXML
    private Button addRestaurantBtn;


    public void initialize() {
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());

        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        followBtn.setOnMouseClicked(mouseEvent -> clickOnFollowBtn(mouseEvent));
        editIcon.setOnMouseClicked(mouseEvent -> clickOnEditIcon(mouseEvent));
        deleteUserBtn.setOnMouseClicked(mouseEvent -> clickOnDeleteUserBtn(mouseEvent));
        bookingBtn.setOnMouseClicked(mouseEvent -> clickOnBookingBtn(mouseEvent));
        addRestaurantBtn.setOnMouseClicked(mouseEvent -> clickOnAddRestaurantBtn(mouseEvent));
    }

    public void setProfilePage(User user) {
        this.user = user;

        // Push
        Session.getInstance().getPreviousPageUsers().add(user);

        username.setText(user.getUsername());
        if (user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            // Update Forced
            user = mongoMan.getUserByUsername(Session.getInstance().getLoggedUser().getUsername());
            Session.getInstance().updateLoggedUserInfo(user);
        }
        this.user = user;
        email.setText(user.getEmail());
        name.setText(user.getName());
        surname.setText(user.getSurname());
        if (user.getAge() != -1)
            age.setText(String.valueOf(user.getAge()));
        else
            age.setText("");
        nFollower.setText(String.valueOf(neoMan.getNumFollowersUser(user.getUsername())));
        nFollowing.setText(String.valueOf(neoMan.getNumFollowingUser(user.getUsername())));
        int nRestaurant = 0;

        if (user.getRestaurantList().isEmpty() == false) {
            nRestaurant = user.getRestaurantList().size();
        }

        nRestaurantAdded.setText(String.valueOf(nRestaurant));

        //Image image = null;
        //URL url = getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/img/user.png");
        //image = new Image(String.valueOf(url));
        //profileImg.setImage(new ImagePattern(image).getImage());


        if (user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            loadMyRestaurantList();
            followBtn.setVisible(false);
            editIcon.setVisible(true);



        } else if (neoMan.userAFollowsUserB(Session.getInstance().getLoggedUser().getUsername(), user.getUsername())) {
            loadMyRestaurantList();
            followBtn.setText("Unfollow");
        } else {
            box.getChildren().clear();
            followBtn.setVisible(true);
            editIcon.setVisible(false);


        }
        if (Session.getInstance().getLoggedUser().getRole() == 1 &&
                user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            addRestaurantBox.setVisible(true);
        } else {
            addRestaurantBox.setVisible(false);
        }
        if (Session.getInstance().getLoggedUser().getRole() == 1 ||
                user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            bookingBtn.setVisible(true);
        } else {
            bookingBtn.setVisible(false);
        }

        if (Session.getInstance().getLoggedUser().getRole() == 1 &&
                !user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            deleteUserBtn.setVisible(true);

        } else {
            deleteUserBtn.setVisible(false);

        }

    }

    private void loadMyRestaurantList() {
        boxLabel.setText("Restaurant List");
        if (!user.getRestaurantList().isEmpty()) {
            Iterator<RestaurantListObject> it = user.getRestaurantList().iterator();

            while (it.hasNext()) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER);
                row.setStyle("-fx-padding: 10px");
                RestaurantListObject r = it.next();
                Pane p = loadRestaurantCard(r, user);

                row.getChildren().addAll(p);
                box.getChildren().add(row);
            }
        } else {
            box.getChildren().add(new Label("No Restaurant List :("));
        }
    }

    private Pane loadRestaurantCard(RestaurantListObject object, User user) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantlistobjectcard.fxml"));
            pane = loader.load();
            RestaurantListObjectCtrl ctrl = loader.getController();
            ctrl.setRestaurantListObjectCard(object, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon(MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPageUsers().remove(Session.getInstance().getPreviousPageUsers().size() - 1);

        // Check if previous page is a Restaurant Page
        if (Session.getInstance().getPreviousPageRestaurant().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/browser.fxml", mouseEvent);
        else {
            RestaurantPage ctrl = (RestaurantPage) Utils.changeScene(
                    "/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantpage.fxml", mouseEvent);
            ctrl.setRestaurantPage(Session.getInstance().getPreviousPageRestaurant().remove(
                    Session.getInstance().getPreviousPageRestaurant().size() - 1));
        }
    }

    private void clickOnFollowBtn(MouseEvent mouseEvent) {
        String tmp = followBtn.getText();
        if (tmp.equals("Follow")) {
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            neoMan.followUser(Session.getInstance().getLoggedUser().getUsername(), user.getUsername(), timestamp);
            followBtn.setText("Unfollow");
            loadMyRestaurantList();
            // Update the n Follower label
            int newNumFollower = Integer.parseInt(nFollower.getText()) + 1;
            nFollower.setText(String.valueOf(newNumFollower));
        } else {
            neoMan.unfollowUser(Session.getInstance().getLoggedUser().getUsername(), user.getUsername());
            followBtn.setText("Follow");
            box.getChildren().clear();
            // Update the n Follower label
            int newNumFollower = Integer.parseInt(nFollower.getText()) - 1;
            nFollower.setText(String.valueOf(newNumFollower));
        }

    }

    private void clickOnEditIcon(MouseEvent mouseEvent) {
        String oldUsername = Session.getInstance().getLoggedUser().getUsername() ;
        /* Edit form */
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile Information");

        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField username = new TextField(Session.getInstance().getLoggedUser().getUsername());
        username.setPromptText("Username");
        TextField name = new TextField(Session.getInstance().getLoggedUser().getName());
        name.setPromptText("Name");
        TextField surname = new TextField(Session.getInstance().getLoggedUser().getSurname());
        surname.setPromptText("Surname");
        TextField age = new TextField(String.valueOf(Session.getInstance().getLoggedUser().getAge()));
        age.setPromptText("Age");
        TextField email = new TextField(Session.getInstance().getLoggedUser().getEmail());
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        dialogPane.setContent(new VBox(8, username, name, surname, age, email, password));
        Platform.runLater(name::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new User(username.getText(),
                        email.getText(),
                        Session.getInstance().getLoggedUser().getPassword(),
                        name.getText(),
                        surname.getText(),
                        Integer.parseInt(age.getText()),
                        Session.getInstance().getLoggedUser().getRestaurantList(),
                        Session.getInstance().getLoggedUser().getRole());
            }
            return null;
        });
        Optional<User> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((User u) -> {
            if (!mongoMan.updateUser(u)) {
                Utils.error();
                return;
            }
            if (!Objects.equals(oldUsername, username.getText()))
                if (!neoMan.updateUser(oldUsername, username.getText())) {
                    // Restore previous information if errors occur
                    mongoMan.updateUser(Session.getInstance().getLoggedUser());
                    Utils.error();
                    return;
                }
            // Refresh Page Content
            Session.getInstance().setLoggedUser(u);
            setProfilePage(u);
        });
    }


    private void clickOnDeleteUserBtn(MouseEvent mouseEvent) {
        if (!mongoMan.deleteUser(user)) {
            Utils.error();
            return;
        }
        if (!neoMan.deleteUser(user)) {
            mongoMan.addUser(user);
            Utils.error();
            return;
        }
        Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/browser.fxml", mouseEvent);
    }

    private void clickOnBookingBtn(MouseEvent mouseEvent) {
        BookingPage ctrl = (BookingPage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/bookingpage.fxml", mouseEvent);

        ctrl.setBookingPageUser(user);
    }

    private void clickOnAddRestaurantBtn(MouseEvent mouseEvent) {
        String name = nameRestaurant.getText();

        if (mongoMan.getRestaurantByName(name) != null) {
            System.out.println("Restaurant already registered");
            return;
        }

        Restaurant newRestaurant = new Restaurant(name, cityRestaurant.getText(), cuisineRestaurant.getText(), new ArrayList<>());

        if (!mongoMan.addRestaurant(newRestaurant)) {
            Utils.error();
            return;
        }
        if (!neoMan.addRestaurant(newRestaurant)) {
            mongoMan.deleteRestaurant(newRestaurant);
            Utils.error();
            return;
        }
    }

}
