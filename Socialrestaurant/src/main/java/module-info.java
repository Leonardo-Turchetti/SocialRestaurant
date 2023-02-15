module it.unipi.dii.lsmd.socialrestaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires com.google.gson;
    requires java.desktop;
    requires org.neo4j.driver;


    opens it.unipi.dii.lsmd.socialrestaurant to javafx.fxml;
    exports it.unipi.dii.lsmd.socialrestaurant;
    exports it.unipi.dii.lsmd.socialrestaurant.controller;
    opens it.unipi.dii.lsmd.socialrestaurant.controller to javafx.fxml;
    exports it.unipi.dii.lsmd.socialrestaurant.database;
    opens it.unipi.dii.lsmd.socialrestaurant.database to javafx.fxml;
    opens it.unipi.dii.lsmd.socialrestaurant.model to com.google.gson;
    exports it.unipi.dii.lsmd.socialrestaurant.model;
}