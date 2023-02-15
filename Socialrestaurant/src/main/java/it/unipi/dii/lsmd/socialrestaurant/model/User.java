package it.unipi.dii.lsmd.socialrestaurant.model;

import java.util.List;

public class User {
    private String name;
    private String surname;
    private String email;
    private String username;
    private String password;
    private int age;

    private int role;

    private List<RestaurantListObject> restaurantList;



    public User(String username, String email, String password, String name, String surname, int age, List<RestaurantListObject> restaurantList, int role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.age = age;
        this.restaurantList = restaurantList;

        this.role = role;
    }

    public User(String username) {
        this(username, null, null, null, null, -1, null, -1);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getAge() {
        return age;
    }

    public List<RestaurantListObject> getRestaurantList() {
        return restaurantList;
    }
    public int getRole() { return this.role; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setRestaurantList(List<RestaurantListObject> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public void setRole(int role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", role=" + role +
                ", restaurantList=" + restaurantList +
                '}';
    }
}
