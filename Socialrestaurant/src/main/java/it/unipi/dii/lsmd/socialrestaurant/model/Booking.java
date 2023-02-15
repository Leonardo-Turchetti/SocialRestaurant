package it.unipi.dii.lsmd.socialrestaurant.model;

public class Booking {

    private String username;
    private String restaurantname;
    private String city;
    private String cuisine;
    private String timestamp;

    public Booking(String username, String restaurantname, String city, String cuisine, String timestamp) {
        this.username = username;
        this.restaurantname = restaurantname;
        this.city = city;
        this.cuisine = cuisine;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }
    public String getRestaurantname() {
        return restaurantname;
    }
    public String getCity() {
        return city;
    }
    public String getCuisine() {
        return cuisine;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setRestaurantname(String name) {
        this.restaurantname = restaurantname;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "username='" + username + '\'' +
                "restaurantname='" + restaurantname + '\'' +
                ", city='" + city + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
