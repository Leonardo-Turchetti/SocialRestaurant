package it.unipi.dii.lsmd.socialrestaurant.model;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private static Session instance = null;
    private User loggedUser;
    private List<User> previousPageUsers;
    private List<Booking> previousPageBooking;
    private List<Restaurant> previousPageRestaurant;



    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private Session () {
        previousPageUsers = new ArrayList<>();
        previousPageBooking = new ArrayList<>();
        previousPageRestaurant = new ArrayList<>();
    }

    public void setLoggedUser(User u) {
        instance.loggedUser = u;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public List<User> getPreviousPageUsers() {
        return previousPageUsers;
    }

    public List<Booking> getPreviousPageBooking() {
        return previousPageBooking;
    }

    public List<Restaurant> getPreviousPageRestaurant() {
        return previousPageRestaurant;
    }

    public void updateLoggedUserInfo(User user) {
        instance.loggedUser = user;
    }
}
