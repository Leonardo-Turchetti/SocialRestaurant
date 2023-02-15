package it.unipi.dii.lsmd.socialrestaurant.model;

import java.util.Date;

public class RestaurantListObject {
    private String name;
    private String city;
    private String cuisine;
    private Date timestamp;

    public RestaurantListObject(String name, String city, String cuisine, Date timestamp) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.timestamp = timestamp;
    }


    public String getName() {
        return name;
    }
    public String getCity() {
        return city;
    }
    public String getCuisine() {
        return cuisine;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RestaurantListObject{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
