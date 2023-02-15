package it.unipi.dii.lsmd.socialrestaurant.model;

import java.util.List;

public class Restaurant {
    private String name;
    private String city;

    private String cuisine;
    private List<Review> reviewList;

    public Restaurant(String name, String city, String cuisine, List<Review> reviewList)
    {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.reviewList = reviewList;
    }



    public String getName() {
        return name;
    }

    public String getCity() { return city; }

    public String getCuisine() {
        return cuisine;
    }

    public List<Review> getReviewList() {
        return reviewList;
    }


    public void setName(String name) { this.name = name; }


    public void setCity(String city) { this.city = city; }


    public void setCuisine(String category) { this.cuisine = cuisine; }

    public void setReviewList(List<Review> reviewList) { this.reviewList = reviewList; }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name +
                ", city='" + city +
                ", cuisine=" + cuisine+
                ", reviewList=" + reviewList +
                '}';
    }
}