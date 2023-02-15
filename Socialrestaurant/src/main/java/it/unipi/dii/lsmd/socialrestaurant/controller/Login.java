package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.model.Session;
import it.unipi.dii.lsmd.socialrestaurant.model.User;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login{

    private MongoDBManager mongoMan;
    @FXML private Button loginButton;
    @FXML private PasswordField passwordTf;
    @FXML private Button registerButton;
    @FXML private TextField usernameTf;
    @FXML private Label errorTf;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
    }

    @FXML
    void checkCredential(ActionEvent event) {
        String username = usernameTf.getText();
        String password = passwordTf.getText();

        User u = mongoMan.login(username, password);

        if (u == null) {
            usernameTf.setText("");
            passwordTf.setText("");
            errorTf.setText("Username or password not valid.");
            System.out.println("Username or password not valid");
        } else {
            Session.getInstance().setLoggedUser(u);
            Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/browser.fxml", event);
        }
    }

    /**
     * If the user click the button register this function
     * will change the app stage and show the register form
     *
     * @param event
     */
    @FXML
    void loadRegisterForm(ActionEvent event) {
        Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/registration.fxml", event);
    }


}