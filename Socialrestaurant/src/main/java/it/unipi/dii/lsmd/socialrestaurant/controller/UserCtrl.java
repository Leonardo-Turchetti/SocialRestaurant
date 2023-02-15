package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.model.User;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class UserCtrl implements Initializable {

    @FXML private ImageView imageProfile;
    @FXML private Label usernameLb;
    @FXML private Text emailTf;
    @FXML private Label analyticLabelName;
    @FXML private Text analyticValue;

    private User user;
    private MongoDBManager mongoMan;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
    }

    public void setParameters (User user, String analyticLabelName, int analyticValue) {
        this.user = user;

        usernameLb.setText(user.getUsername());
        emailTf.setText(user.getEmail());

        //Image image = null;
        //URL url = getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/img/user.png");
        //image = new Image(String.valueOf(url));
        //imageProfile.setFill(new ImagePattern(image));

        if (analyticLabelName != null) {
            this.analyticLabelName.setText(analyticLabelName);
            this.analyticValue.setText(String.valueOf(analyticValue));
        }
        else {
            this.analyticLabelName.setVisible(false);
            this.analyticValue.setVisible(false);
        }
    }

    @FXML
    void showProfile(MouseEvent event) {
        ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", event);

        // If user object is a snap, load the complete user object
        if (user.getPassword() == null || user.getRestaurantList() == null)
            user = mongoMan.getUserByUsername(user.getUsername());

        ctrl.setProfilePage(user);
    }
}
