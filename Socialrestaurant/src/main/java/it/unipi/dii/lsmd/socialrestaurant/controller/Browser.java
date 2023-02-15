package it.unipi.dii.lsmd.socialrestaurant.controller;

import it.unipi.dii.lsmd.socialrestaurant.database.MongoDBManager;
import it.unipi.dii.lsmd.socialrestaurant.database.MongoDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jDriver;
import it.unipi.dii.lsmd.socialrestaurant.database.Neo4jManager;
import it.unipi.dii.lsmd.socialrestaurant.model.*;
import it.unipi.dii.lsmd.socialrestaurant.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class Browser implements Initializable {

    @FXML
    private Button backBt;
    @FXML
    private ComboBox<String> chooseCategory;
    @FXML
    private ComboBox<String> chooseCity;
    @FXML
    private ComboBox<String> chooseType;
    @FXML
    private Button forwardBt;
    @FXML
    private Button specialSearchBt;
    @FXML
    private DatePicker fromDate;
    @FXML
    private TextField keywordTf;
    @FXML
    private Button searchBt;
    @FXML
    private DatePicker toDate;
    @FXML
    private HBox cityContainer;
    @FXML
    private HBox categoryContainer;
    @FXML
    private HBox keywordContainer;
    @FXML
    private HBox dateContainer;
    @FXML
    private Label errorTf;
    @FXML
    private GridPane cardsGrid;
    @FXML
    private CheckBox followsCheckBox;
    @FXML
    private HBox followsContainer;
    @FXML
    private HBox paramContainer;
    @FXML
    private HBox timeRangeContainer;
    @FXML
    private ComboBox<String> chooseTarget;
    @FXML
    private ComboBox<String> chooseQuery;
    @FXML
    private ComboBox<String> chooseTimeRange;

    private MongoDBManager mongoManager;
    private Neo4jManager neo4jManager;
    private User user;
    private int page;
    private int special;

    @FXML
    void goToProfilePage(MouseEvent event) {
        ProfilePage ctrl = (ProfilePage) Utils.changeScene(
                "/it/unipi/dii/lsmd/socialrestaurant/layout/profilepage.fxml", event);
        if (ctrl != null) {
            ctrl.setProfilePage(Session.getInstance().getLoggedUser());
        }
    }

    @FXML
    private void logout(MouseEvent event) {
        Session.resetInstance();
        Utils.changeScene("/it/unipi/dii/lsmd/socialrestaurant/layout/login.fxml", event);
    }

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        mongoManager = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neo4jManager = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        user = Session.getInstance().getLoggedUser();
        special = 0;
        loadComboBox();
        hideFilterForm();
        forwardBt.setOnMouseClicked(mouseEvent -> goForward());
        backBt.setOnMouseClicked(mouseEvent -> goBack());
    }

    // -------------------------------------------- NORMAL RESEARCH --------------------------------------------
    @FXML
    void switchForm() {
        searchBt.setDisable(false);
        backBt.setDisable(true);
        forwardBt.setDisable(true);
        followsCheckBox.setSelected(false);
        special = 0;
        switch (chooseType.getValue()) {
            case "Restaurant" -> {
                cityContainer.setVisible(true);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(true);
                categoryContainer.setVisible(true);
                followsContainer.setVisible(false);
            }
            case "Users" -> {
                cityContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(true);
                followsContainer.setVisible(true);
                categoryContainer.setVisible(false);
            }
            case "Booking" -> {
                cityContainer.setVisible(true);
                dateContainer.setVisible(true);
                keywordContainer.setVisible(false);
                followsContainer.setVisible(false);
                categoryContainer.setVisible(true);
            }
            case "Moderate reviews" -> {
                cityContainer.setVisible(false);
                dateContainer.setVisible(true);
                keywordContainer.setVisible(false);
                followsContainer.setVisible(false);
                categoryContainer.setVisible(false);
            }
            default -> {
                cityContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(false);
                categoryContainer.setVisible(false);
                followsContainer.setVisible(false);
            }
        }
    }

    @FXML
    void startResearch() {
        forwardBt.setDisable(false);
        backBt.setDisable(true);
        page = 0;
        special = 0;
        specialSearchBt.setDisable(true);
        handleResearch();
    }

    private void handleResearch() {
        special = 0;
        switch (chooseType.getValue()) {
            case "Restaurant" -> {
                // check the form values
                errorTf.setText("");
                if (keywordTf.getText().equals("")  &&
                        (chooseCategory.getValue() == null || chooseCategory.getValue().equals("Select cuisine"))
                        && (chooseCity.getValue() == null || chooseCity.getValue().equals("Select cuisine"))) {
                    errorTf.setText("You have to set some filters.");
                    forwardBt.setDisable(true);
                    return;
                }
                String cuisine = "";
                if (chooseCategory.getValue() != null && !chooseCategory.getValue().equals("Select cuisine"))
                    cuisine = chooseCategory.getValue();
                String city = "";
                if (chooseCity.getValue() != null && !chooseCity.getValue().equals("Select city"))
                    city = chooseCity.getValue();
                // handle cards display
                // load papers
                List<Restaurant> restaurantList = mongoManager.searchRestaurantByParameters(keywordTf.getText(), city,
                        cuisine, 3*page, 3);
                fillRestaurants(restaurantList);
            }
            case "Users" -> {
                // form control
                errorTf.setText("");
                if (keywordTf.getText().equals("") && !followsCheckBox.isSelected()) {
                    errorTf.setText("You have to specify an option.");
                    return;
                }
                List<User> usersList;
                // check if you need follows
                if(followsCheckBox.isSelected()) {
                    usersList = neo4jManager.getSnapsOfFollowedUserByKeyword(user, keywordTf.getText(), 8, 8 * page);
                    for(User u : usersList){
                        String email = mongoManager.getUserByUsername(u.getUsername()).getEmail();
                        u.setEmail(email);
                    }
                }else {
                    usersList = mongoManager.getUsersByKeyword(keywordTf.getText(), false, page);
                }
                fillUsers(usersList);
            }
            case "Booking" -> {
                // form control
                errorTf.setText("");
                if (toDate.getValue() != null && fromDate.getValue() != null &&
                        toDate.getValue().isBefore(fromDate.getValue())) {
                    errorTf.setText("The From date have to be before the To date.");
                    forwardBt.setDisable(true);
                    return;
                }
                String cuisine = "";
                if (chooseCategory.getValue() != null && !chooseCategory.getValue().equals("Select cuisine"))
                    cuisine = chooseCategory.getValue();
                String city = "";
                if (chooseCity.getValue() != null && !chooseCity.getValue().equals("Select city"))
                    city = chooseCity.getValue();

                String startDate = "";
                String endDate = "";

                if (toDate.getValue() != null)
                    endDate = toDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (fromDate.getValue() != null)
                    startDate = fromDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // load booking
                List<Booking> bookingList = mongoManager.searchBookingByParameters(city,cuisine,startDate,endDate, 3*page, 3);
                fillBooking(bookingList);
            }
            case "Moderate reviews" -> {
                // form control
                errorTf.setText("");
                if (toDate.getValue() != null && fromDate.getValue() != null &&
                        toDate.getValue().isBefore(fromDate.getValue())) {
                    errorTf.setText("The From date have to be before the To date.");
                    forwardBt.setDisable(true);
                    return;
                }

                String startDate = "";
                String endDate = "";

                if (toDate.getValue() != null)
                    endDate = toDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (fromDate.getValue() != null)
                    startDate = fromDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                fillReviews(startDate, endDate);
            }
        }
    }

    // -------------------------------------------- SPECIAL RESEARCH --------------------------------------------


    @FXML
    void showOption() {
        page = 0;
        special = 1;
        cleanGrid();
        paramContainer.setVisible(false);
        timeRangeContainer.setVisible(false);
        //chooseTarget.getItems().clear();
        chooseTarget.setPromptText("Select option");
        forwardBt.setDisable(true);
        backBt.setDisable(true);
        specialSearchBt.setDisable(true);
        special = 0;
        switch (chooseQuery.getValue()) {
            case "Suggestions" -> {
                List<String> typeList1 = new ArrayList<>();
                typeList1.add("Restaurant");
                typeList1.add("Users");
                ObservableList<String> observableListType1 = FXCollections.observableList(typeList1);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType1);
                chooseTarget.setPromptText("Select option");
                paramContainer.setVisible(true);
                timeRangeContainer.setVisible(false);
                chooseTarget.setDisable(false);
            }
            case "Analytics" -> {
                List<String> typeList2 = new ArrayList<>();
                typeList2.add("Most commented restaurant");
                typeList2.add("Most liked restaurant");
                typeList2.add("Most followed users");
                ObservableList<String> observableListType2 = FXCollections.observableList(typeList2);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType2);
                chooseTarget.setPromptText("Select option");
                paramContainer.setVisible(true);
                chooseTarget.setDisable(false);
            }
            case "Summary" -> {
                List<String> typeList = new ArrayList<>();
                typeList.add("Categories by likes");
                typeList.add("Categories by reviews");
                typeList.add("Cities by booking");
                ObservableList<String> observableListType = FXCollections.observableList(typeList);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType);
                chooseTarget.setPromptText("Select option");
                paramContainer.setVisible(true);
                timeRangeContainer.setVisible(true);
                chooseTarget.setDisable(false);
            }
        }
    }

    @FXML
    void selectedOption() {
        if (chooseTarget.getValue() != null) {
            switch (chooseTarget.getValue()) {
                case "Most followed users",
                        "Restaurant", "Users" -> {
                    timeRangeContainer.setVisible(false);
                    specialSearchBt.setDisable(false);
                }
                default -> timeRangeContainer.setVisible(true);
            }
        }

    }

    @FXML
    void periodSelected() {
        specialSearchBt.setDisable(false);
    }

    @FXML
    void startSpecialSearch() {
        cleanGrid();
        forwardBt.setDisable(false);
        searchBt.setDisable(true);
        errorTf.setText("");
        special = 1;
        switch (chooseQuery.getValue()) {
            case "Suggestions" -> {
                if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                }
                switch (chooseTarget.getValue()) {
                    case "Users" -> {
                        List<User> suggestedUser = neo4jManager.getSnapsOfSuggestedUsers(user, 4,4, 4*page, 4*page);
                        fillUsers(suggestedUser);
                    }
                    case "Restaurant" -> {
                        List<Restaurant> suggestedRestaurant = neo4jManager.getSnapsOfSuggestedRestaurants(user, 2, 1, 2*page, 1*page);
                        fillRestaurants(suggestedRestaurant);
                    }
                }
            }
            case "Summary" -> {
                forwardBt.setDisable(true);
                if (chooseTimeRange.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                } else if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                } else {
                    String period = chooseTimeRange.getValue().toLowerCase(Locale.ROOT);
                    List<Pair<String, Integer>> list;
                    switch (chooseTarget.getValue()) {
                        case "Categories by likes" -> {
                            list = neo4jManager.getCategoriesSummaryByLikes(period);
                            categoriesTableView(list, "Likes");
                        }
                        case "Categories by reviews" -> {
                            list = mongoManager.getCategoriesSummaryByComments(period);
                            categoriesTableView(list, "Reviews");
                        }
                        case "Cities by booking" -> {
                            list = mongoManager.getCitiesSummaryByBooking(period);
                            categoriesTableView(list, "Booking");
                        }
                    }
                }
            }
            case "Analytics" -> {
                if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                }
                if (!timeRangeContainer.isVisible()) {
                    switch (chooseTarget.getValue()) {
                        case "Most followed users" -> {
                            List<Pair<User, Integer>> users = neo4jManager.getMostFollowedUsers(8*page, 8);
                            fillUsers(users, "Follower");
                        }
                    }
                } else {
                    String period = chooseTimeRange.getValue().toLowerCase(Locale.ROOT);
                    switch (chooseTarget.getValue()) {
                        case "Most commented restaurant" -> {
                            List<Pair<Restaurant, Integer>> restaurants = mongoManager.getMostCommentedRestaurants(period, 3*page, 3);
                            fillRestaurants(restaurants, "Reviews");
                        }
                        case "Most liked restaurant" -> {
                            List<Pair<Restaurant, Integer>> restaurants = neo4jManager.getMostLikedRestaurants(period, 3*page, 3);
                            fillRestaurants(restaurants, "Likes");
                        }
                    }
                }
            }
        }
    }
    // -------------------------------------------- UTILS --------------------------------------------

    private Pane loadUsersCard (User user, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/layout/usercard.fxml"));
            pane = loader.load();
            UserCtrl ctrl = loader.getController();
            ctrl.setParameters(user, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadRestaurantsCard (Restaurant restaurant, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/layout/restaurantcard.fxml"));
            pane = loader.load();
            RestaurantCtrl ctrl = loader.getController();
            ctrl.setRestaurantCard(restaurant, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadBookingsCard (Booking booking, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/layout/bookingcard.fxml"));
            pane = loader.load();
            BookingCtrl ctrl = loader.getController();
            ctrl.setBookingCard(booking, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }
    private Pane loadReviewCard (Review review, Restaurant restaurant) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/socialrestaurant/layout/reviewcard.fxml"));
            pane = loader.load();
            ReviewCtrl ctrl = loader.getController();
            ctrl.setReviewCard(review, restaurant, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void loadComboBox () {
        // load suggestion
        List<String> suggestionList = new ArrayList<>();
        suggestionList.add("Suggestions");
        if(user.getRole()>0) {
            suggestionList.add("Analytics");
            suggestionList.add("Summary");
        }
        ObservableList<String> observableListSuggestion = FXCollections.observableList(suggestionList);
        chooseQuery.getItems().clear();
        chooseQuery.setItems(observableListSuggestion);

        List<String> timeRange = new ArrayList<>();
        timeRange.add("Week");
        timeRange.add("Month");
        timeRange.add("All");
        ObservableList<String> observableListTimeRange = FXCollections.observableList(timeRange);
        chooseTimeRange.getItems().clear();
        chooseTimeRange.setItems(observableListTimeRange);

        // load type
        List<String> typeList = new ArrayList<>();
        typeList.add("Restaurant");
        typeList.add("Users");
        if (user.getRole() > 0){
            typeList.add("Booking");
            typeList.add("Moderate reviews");
        }

        ObservableList<String> observableListType = FXCollections.observableList(typeList);
        chooseType.getItems().clear();
        chooseType.setItems(observableListType);

        // load categories
        List<String> categoriesList = mongoManager.getCategoriesCuisine();
        categoriesList.add(0, "Select category");
        ObservableList<String> observableListCategories = FXCollections.observableList(categoriesList);
        chooseCategory.getItems().clear();
        chooseCategory.setItems(observableListCategories);

        // load city
        List<String> cityList = mongoManager.getCategoriesCity();
        cityList.add(0, "Select city");
        ObservableList<String> observableListCity = FXCollections.observableList(cityList);
        chooseCity.getItems().clear();
        chooseCity.setItems(observableListCity);
    }

    private void hideFilterForm() {
        cityContainer.setVisible(false);
        dateContainer.setVisible(false);
        keywordContainer.setVisible(false);
        categoryContainer.setVisible(false);
        followsContainer.setVisible(false);
    }

    private void fillUsers(List<User> usersList) {
        // set new layout
        setGridUsers();
        if (usersList.size() != 8)
            forwardBt.setDisable(true);
        int row = 0;
        int col = 0;
        for (User u : usersList) {
            Pane card = loadUsersCard(u, null, 0);
            cardsGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void fillUsers(List<Pair<User, Integer>> usersList, String label) {
        // set new layout
        setGridUsers();
        if (usersList.size() != 8)
            forwardBt.setDisable(true);
        int row = 0;
        int col = 0;
        for (Pair<User, Integer> u : usersList) {
            Pane card = loadUsersCard(u.getKey(), label, u.getValue());
            cardsGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void fillRestaurants(List<Restaurant> restaurantList) {
        setGridRestaurants();
        if (restaurantList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Restaurant r : restaurantList) {
            Pane card = loadRestaurantsCard(r, null, 0 );
            cardsGrid.add(card, 0, row);
            row++;
        }
    }


    private void fillBooking(List<Booking> bookingList) {
        setGridRestaurants();
        if (bookingList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Booking b : bookingList) {
            Pane card = loadBookingsCard(b, null, 0 );
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillRestaurants(List<Pair<Restaurant, Integer>> papersList, String label) {
        setGridRestaurants();
        if (papersList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<Restaurant, Integer> p : papersList) {
            Pane card = loadRestaurantsCard(p.getKey(), label, p.getValue());
            cardsGrid.add(card, 0, row);
            row++;
        }
    }


        private void fillReviews(String start_date, String end_date) {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setHgap(5);
        cardsGrid.setPadding(new Insets(15,40,15,120));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);

        // load reviews
        List<Pair<Restaurant, Review>> reviewList = mongoManager.searchLastReviews(start_date, end_date, 4*page, 4);
        if (reviewList.size() != 4)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<Restaurant, Review> cardInfo : reviewList) {
            Pane card = loadReviewCard(cardInfo.getValue(), cardInfo.getKey());
            cardsGrid.add(card, 0, row);
            row++;
            if(row == 4)
                row = 0;
        }
    }

    private void goForward () {
        page++;
        backBt.setDisable(false);
        switch (special) {
            case 0 -> handleResearch();
            default -> startSpecialSearch();
        }
    }

    private void goBack () {
        page--;
        if (page <= 0) {
            page = 0;
            backBt.setDisable(true);
        }
        forwardBt.setDisable(false);
        switch (special) {
            case 0 -> handleResearch();
            default -> startSpecialSearch();
        }
    }

    private void setGridUsers() {
        cleanGrid();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(15,40,15,40));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(25);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void setGridRestaurants() {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(25);
        cardsGrid.setPadding(new Insets(15,40,15,100));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void setGridReadingList() {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(15,40,15,160));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void cleanGrid() {
        cardsGrid.getColumnConstraints().clear();
        while (cardsGrid.getChildren().size() > 0) {
            cardsGrid.getChildren().remove(0);
        }
    }

    private void categoriesTableView(List<Pair<String, Integer>> list, String value) {
        cleanGrid();
        TableView table = new TableView();
        TableColumn firstColumn = new TableColumn("Categories");
        firstColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn secondColumn = new TableColumn(value);
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        table.getColumns().addAll(firstColumn, secondColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for(Pair<String, Integer> row : list) {
            table.getItems().add(row);
        }
        // append the table to a scrollable (???)
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,120));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);

        cardsGrid.add(table, 0, 0);
    }

}