package it.unipi.dii.lsmd.socialrestaurant.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import it.unipi.dii.lsmd.socialrestaurant.model.*;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

/**
 * MongoDB Queries Managers
 */
public class MongoDBManager {
    public MongoDatabase db;
    private MongoCollection usersCollection;
    private MongoCollection restaurantCollection;

    private MongoCollection bookingCollection;

    public MongoDBManager(MongoClient client) {
        this.db = client.getDatabase("SocialRestaurant");
        usersCollection = db.getCollection("user");
        restaurantCollection = db.getCollection("restaurant");
        bookingCollection = db.getCollection("booking");
    }

    /**
     * Method used to perform the login
     *
     * @param username User that is logging in
     * @param password Password User
     * @return User informations related to the username
     */
    public User login(String username, String password) {
        try{
        Document result = (Document) usersCollection.find(Filters.and(eq("username", username),
                        eq("password", password))).
                first();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        return gson.fromJson(gson.toJson(result), User.class);
    }
        catch (JsonSyntaxException e) {
        e.printStackTrace();
        return null;
    }
    }

    /**
     * Add a new User to MongoDB
     *
     * @param u The object User which contains all the necessary information
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addUser(User u) {
        try {
            Document doc = new Document("username", u.getUsername())
                    .append("email", u.getEmail())
                    .append("password", u.getPassword());

            if (u.getName() != null)
                doc.append("name", u.getName());
            if (u.getSurname() != null)
                doc.append("surname", u.getSurname());
            if (u.getAge() != -1)
                doc.append("age", u.getAge());

            doc.append("restaurantList", u.getRestaurantList());

            usersCollection.insertOne(doc);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addRestaurant(Restaurant r) {
        try {
            Document doc = new Document("name", r.getName())
                    .append("city", r.getCity())
                    .append("cuisine", r.getCuisine());

            doc.append("reviewList", r.getReviewList());

            restaurantCollection.insertOne(doc);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that deletes the user from the database
     *
     * @param u user to delete
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean deleteUser(User u) {
        try {
            Bson find = eq("reviewList.username", u.getUsername());
            Bson update = Updates.set("reviewList.$.username", "Deleted user");
            restaurantCollection.updateMany(find, update);
            usersCollection.deleteOne(eq("username", u.getUsername()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRestaurant(Restaurant r) {
        try {
            Bson find = eq("restaurantList.name", r.getName());
            Bson update = Updates.set("restaurantList.$.name", "Deleted restaurant");
            usersCollection.updateMany(find, update);
            restaurantCollection.deleteOne(eq("name", r.getName()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that deletes the user from the database
     *
     * @param object booking to delete
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean removeBooking(Booking object) {
        try {
            bookingCollection.deleteOne(and(eq("username", object.getUsername()),
                                            eq("restaurantname", object.getRestaurantname()),
                                            eq("timestamp", object.getTimestamp())));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Edit an already present user
     *
     * @param u the new user information to replace the old one
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean updateUser(User u) {
        try {
            Document doc = new Document().append("username", u.getUsername());
            if (!u.getEmail().isEmpty())
                doc.append("email", u.getEmail());
            if (!u.getPassword().isEmpty())
                doc.append("password", u.getPassword());
            if (!u.getName().isEmpty())
                doc.append("name", u.getName());
            if (!u.getSurname().isEmpty())
                doc.append("surname", u.getSurname());
            if (u.getAge() != -1)
                doc.append("age", u.getAge());
            doc.append("role", u.getRole());

            Bson updateOperation = new Document("$set", doc);
            usersCollection.updateOne(new Document("username", u.getUsername()), updateOperation);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Method that searches a user by his username
     *
     * @param username username of the user
     * @return User
     */
    public User getUserByUsername(String username) {
        Document result = (Document) usersCollection.find((eq("username", username))).first();
        if (result == null) {
            return null;
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        return gson.fromJson(gson.toJson(result), User.class);
    }


    /**
     * Method that adds a review to a restaurant
     *
     * @param restaurant Restaurant Object
     * @param review     text of the review
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addReview(Restaurant restaurant, Review review) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Document doc = new Document("username", review.getUsername())
                    .append("text", review.getText())
                    .append("timestamp", dateFormat.format(review.getTimestamp()));

            Bson find = eq("name", restaurant.getName());
            Bson update = Updates.addToSet("reviewList", doc);
            restaurantCollection.updateOne(find, update);
            return true;
        } catch (Exception e) {
            System.out.println("Error in adding a review to a Restaurant");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method that adds a review to a restaurant
     *
     * @param booking Booking Object
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addBooking(Booking booking) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Document doc = new Document("username", booking.getUsername())
                    .append("restaurantname", booking.getRestaurantname())
                    .append("city", booking.getCity())
                    .append("cuisine", booking.getCuisine())
                    .append("timestamp", booking.getTimestamp());

            bookingCollection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error in adding a booking");
            e.printStackTrace();
            return false;
        }
    }

    public Booking checkBooking(String username,String restaurantname,String timestamp) {
        try {
            Document result = (Document) bookingCollection.find(and(eq("username", username),
                                                                    eq("restaurantname", restaurantname),
                                                                    eq("timestamp", timestamp))).first();
            if (result == null) {
                return null;
            }

            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            return gson.fromJson(gson.toJson(result), Booking.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Restaurant getRestaurantByName(String name) {
        try {
            Document result = (Document) restaurantCollection.find((eq("name", name))).first();
            if (result == null) {
                return null;
            }

            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            return gson.fromJson(gson.toJson(result), Restaurant.class);
        }
        catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method that updates an existing review
     *
     * @param restaurant Restaurant Object
     * @param review     review
     */
    public void updateReview(Restaurant restaurant, Review review) {
        List<Review> reviewList = restaurant.getReviewList();
        int i = 0;
        for (Review r : reviewList
        ) {
            if (r.getUsername().equals(review.getUsername()) && r.getTimestamp().equals(
                    review.getTimestamp())) {
                reviewList.set(i, review);
                break;
            }
            i++;
        }
        updateReviewList(restaurant, reviewList);
    }

    /**
     * Method that deletes a review
     *
     * @param restaurant Restaurant Object
     * @param review     Review Object
     */
    public void deleteReview(Restaurant restaurant, Review review) {
        List<Review> reviewList = restaurant.getReviewList();
        int n = 0;
        int d = 0;
        for (Review r : reviewList) {
            if (r.getTimestamp().equals(review.getTimestamp()) && r.getUsername().equals(review.getUsername())) {
                d = n;
                break;
            }
            n++;
        }
        reviewList.remove(d);
        /*if (Session.getInstance().getLoggedUser().getType() > 0)
            incrementDeletedCommentsCounter(review.getUsername());*/
        updateReviewList(restaurant, reviewList);
    }

    /**
     * Method that updates the list of review of a restaurant
     *
     * @param restaurant Restaurant Object
     * @param reviewList List of the reviews
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean updateReviewList(Restaurant restaurant, List<Review> reviewList) {
        try {
            Bson update = new Document("reviewList", reviewList);
            Bson updateOperation = new Document("$set", update);
            restaurantCollection.updateOne(new Document("name", restaurant.getName()), updateOperation);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error in updating user on MongoDB");
            return false;
        }
    }

    /**
     * Method that adds a Restaurant to a RestaurantList
     *
     * @param user user of the RestaurantList
     * @param r    Restaurant
     * @return true if the operation is successfully executed, false otherwise
     */
    public boolean addRestaurantToRestaurantList(String user, RestaurantListObject r) {
        try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Document doc = new Document("name", r.getName())
                .append("city", r.getCity())
                .append("cuisine", r.getCuisine())
                .append("timestamp", dateFormat.format(r.getTimestamp()));
        Bson find = eq("username", user);
        Bson update = Updates.addToSet("restaurantList", doc);
        usersCollection.updateOne(find, update);
        return true;
    }catch(Exception e){
        System.out.println("Error in adding a restaurant to a User restaurantList");
        e.printStackTrace();
        return false;
    }

}
    /**
     * Method that remove a Restaurant from a RestaurantList
     * @param user user of the RestaurantList
     * @param object Restaurant
     * @return true if the operation is successfully executed, false otherwise
     */
    public void removeRestaurantFromRestaurantList(User user, Restaurant object) {
        List<RestaurantListObject> restaurantList = user.getRestaurantList();
        int n = 0;
        int d = 0;
        for (RestaurantListObject r : restaurantList) {
            if (r.getName().equals(object.getName())) {
                d = n;
                break;
            }
            n++;
        }
        restaurantList.remove(d);
        updateRestaurantList(user, restaurantList);
    }

    /**
     * Method that updates the list of restaurant of a user
     *
     * @param user User Object
     * @param restaurantList List of the restaurant
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean updateRestaurantList(User user, List<RestaurantListObject> restaurantList) {
        try {
            Bson update = new Document("restaurantList", restaurantList);
            Bson updateOperation = new Document("$set", update);
            usersCollection.updateOne(new Document("username", user.getUsername()), updateOperation);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error in updating user on MongoDB");
            return false;
        }
    }

    public List<String> getCategoriesCuisine() {
        List<String> categoriesList = new ArrayList<>();
        restaurantCollection.distinct("cuisine", String.class).into(categoriesList);
        return categoriesList;
    }

    public List<String> getCategoriesCity() {
        List<String> categoriesList = new ArrayList<>();
        restaurantCollection.distinct("city", String.class).into(categoriesList);
        return categoriesList;
    }

    /**
     * Method that searches restaurants given parameters.
     * @param name partial title of the restaurants to match
     * @param city
     * @param cuisine
     * @return a list of restaurants that match the parameters
     */
    public List<Restaurant> searchRestaurantByParameters (String name, String city, String cuisine, int skip, int limit) {
        List<Restaurant> restaurants = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

        List<Bson> pipeline = new ArrayList<>();

        if (!name.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("name", pattern1)));
        }

        if (!city.isEmpty()) {
            Pattern pattern2 = Pattern.compile("^.*" + city + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("city", pattern2)));
        }

        if (!cuisine.isEmpty()) {
            Pattern pattern3 = Pattern.compile("^.*" + cuisine + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.eq("cuisine", pattern3)));
        }

        pipeline.add(skip(skip * limit));
        pipeline.add(limit(limit));

        List<Document> results = (List<Document>) restaurantCollection.aggregate(pipeline).into(new ArrayList<>());
        Type restaurantListType = new TypeToken<ArrayList<Restaurant>>(){}.getType();
        restaurants = gson.fromJson(gson.toJson(results), restaurantListType);
        return restaurants;
    }

    public List<Booking> searchBookingByParameters (String city, String cuisine, String startDate, String endDate, int skip, int limit) {
        List<Booking> bookingList = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

        List<Bson> pipeline = new ArrayList<>();

        if (!city.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^.*" + city + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("city", pattern1)));
        }

        if (!cuisine.isEmpty()) {
            Pattern pattern2 = Pattern.compile("^.*" + city + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("cuisine", pattern2)));
        }

        if (!cuisine.isEmpty()) {
            Pattern pattern3 = Pattern.compile("^.*" + cuisine + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.eq("cuisine", pattern3)));
        }

        if(!startDate.isEmpty())
            pipeline.add(Aggregates.match(gte("timestamp", startDate)));
        if(!endDate.isEmpty())
            pipeline.add(Aggregates.match(lte("timestamp", endDate)));

        pipeline.add(sort(ascending("timestamp")));
        pipeline.add(skip(skip * limit));
        pipeline.add(limit(limit));

        List<Document> results = (List<Document>) bookingCollection.aggregate(pipeline).into(new ArrayList<>());
        Type bookingListType = new TypeToken<ArrayList<Booking>>(){}.getType();
        bookingList = gson.fromJson(gson.toJson(results), bookingListType);
        return bookingList;
    }

    /**
     * Method that searches a booking by the username of a user
     *
     * @param username username of the user
     * @return User
     */
    public List<Booking> getBookingListByUser(String username) {
        List<Booking> bookingList = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

        List<Bson> pipeline = new ArrayList<>();

        if (!username.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^.*" + username + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("username", pattern1)));
            pipeline.add(sort(descending("timestamp")));
        }

        List<Document> results = (List<Document>) bookingCollection.aggregate(pipeline).into(new ArrayList<>());
        Type bookingListType = new TypeToken<ArrayList<Booking>>(){}.getType();
        bookingList = gson.fromJson(gson.toJson(results), bookingListType);


        return bookingList;
    }

    /**
     * Method that searches a booking by the name of a restaurant
     *
     * @param name name of the restaurant
     * @return User
     */
    public List<Booking> getBookingListByRestaurant(String name) {
        List<Booking> bookingList = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        LocalDate todayDate = LocalDate.now();

        List<Bson> pipeline = new ArrayList<>();

        if (!name.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(match(Filters.regex("restaurantname", pattern1)));
            pipeline.add(sort(descending("timestamp")));
        }

        List<Document> results = (List<Document>) bookingCollection.aggregate(pipeline).into(new ArrayList<>());
        Type bookingListType = new TypeToken<ArrayList<Booking>>(){}.getType();
        bookingList = gson.fromJson(gson.toJson(results), bookingListType);


        return bookingList;
    }

    

    /**
     * Return users the contains the keyword, if we give a list of user
     * the research is added only inside this sublist
     * @param next select the portion of result
     * @param keyword keyword to search users
     * @return list of users
     */
    public List<User> getUsersByKeyword (String keyword, boolean admin, int next) {
        List<User> results = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        Consumer<Document> convertInUser = doc -> {
            User user = gson.fromJson(gson.toJson(doc), User.class);
            results.add(user);
        };
        Pattern pattern= Pattern.compile("^.*" + keyword + ".*$", Pattern.CASE_INSENSITIVE);
        Bson filter = match(Filters.regex("username", pattern));
        Bson limit = limit(8);
        Bson skip = skip(next*8);
        if (admin) {
            Bson adminFilter = match(eq("role", 1));
            usersCollection.aggregate(Arrays.asList(filter, adminFilter, skip, limit)).forEach(convertInUser);
        } else
            usersCollection.aggregate(Arrays.asList(filter, skip, limit)).forEach(convertInUser);
        return results;
    }


    /**
     * Browse all comments that has been written "numDays" ago
     * @param start_date start date
     * @param start_date finish date
     * @param skipDoc how many comments skip
     * @param limitDoc limit number of comments
     * @return list of comments
     */
    public List<Pair<Restaurant, Review>> searchLastReviews(String start_date, String end_date, int skipDoc, int limitDoc) {

        List<Pair<Restaurant, Review>> results = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Bson> pipeline = new ArrayList<>();

        Consumer<Document> takeReviews = doc -> {

            String name = (String) doc.get("name");
            String city = (String) doc.get("city");
            String cuisine = (String) doc.get("cuisine");
            Document docReviews = (Document) doc.get("reviewList");
            Review review = gson.fromJson(gson.toJson(docReviews), Review.class);
            Restaurant restaurant = new Restaurant(name, city, cuisine, null );

            results.add(new Pair<>(restaurant, review));
        };

        pipeline.add(Aggregates.unwind("$reviewList"));
        if(!start_date.isEmpty())
            pipeline.add(Aggregates.match(gte("reviewList.timestamp", start_date)));
        if(!end_date.isEmpty())
            pipeline.add(Aggregates.match(lte("reviewList.timestamp", end_date)));
        pipeline.add(sort(ascending("reviewList.timestamp", "reviewList.username")));
        pipeline.add(skip(skipDoc));
        pipeline.add(limit(limitDoc));

        restaurantCollection.aggregate(pipeline).forEach(takeReviews);
        return results;
    }

    /**
     * Browse the top categories with more comments
     * @param period (all, month, week)
     * @return HashMap with the category and the number of comments
     */
    public List<Pair<String, Integer>> getCategoriesSummaryByComments(String period) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Pair<String, Integer>> results = new ArrayList<>();
        Consumer<Document> rankCategories = doc ->
                results.add(new Pair<>((String) doc.get("_id"), (Integer) doc.get("tots")));

        Bson unwind = unwind("$reviewList");
        Bson filter = match(gte("reviewList.timestamp", filterDate));
        Bson group = group("$cuisine", sum("tots", 1));
        Bson sort = sort(Indexes.descending("tots"));
        restaurantCollection.aggregate(Arrays.asList(unwind, filter, group, sort)).forEach(rankCategories);

        return results;
    }

    /**
     * Browse the top categories with more comments
     * @param period (all, month, week)
     * @return HashMap with the category and the number of booking
     */
    public List<Pair<String, Integer>> getCitiesSummaryByBooking(String period) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Pair<String, Integer>> results = new ArrayList<>();
        Consumer<Document> rankCategories = doc ->
                results.add(new Pair<>((String) doc.get("_id"), (Integer) doc.get("tots")));

        Bson unwind = unwind("$timestamp");
        Bson filter = match(gte("timestamp", filterDate));
        Bson group = group("$city", sum("tots", 1));
        Bson sort = sort(Indexes.descending("tots"));
        bookingCollection.aggregate(Arrays.asList(unwind, filter, group, sort)).forEach(rankCategories);

        return results;
    }


    /**
     * Method that returns restaurants with the highest number of comments in the specified period of time.
     * @param period (all, month, week)
     * @param skipDoc (positive integer)
     * @param limitDoc (positive integer)
     * @return HashMap with the title and the number of comments
     */
    public List<Pair<Restaurant, Integer>> getMostCommentedRestaurants(String period, int skipDoc, int limitDoc) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Pair<Restaurant, Integer>> results = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        Consumer<Document> convertInRestaurant = doc -> {
            Restaurant restaurant = gson.fromJson(gson.toJson(doc), Restaurant.class);
            results.add(new Pair(restaurant, doc.getInteger("totalComments")));
        };

        Bson unwind = unwind("$reviewList");
        Bson filter = match(gte("reviewList.timestamp", filterDate));
        Bson group = new Document("$group",
                new Document("_id",
                        new Document("name", "$name")
                                .append("city", "$city")
                                .append("cuisine", "$cuisine"))
                        .append("totalComments",
                                new Document("$sum", 1)));
        Bson project = project(fields(excludeId(),
                computed("name", "$_id.name"),
                computed("city", "$_id.city"),
                computed("cuisine", "$_id.cuisine"),
                include("totalComments")));
        Bson sort = sort(Indexes.descending("totalComments"));
        Bson skip = skip(skipDoc);
        Bson limit = limit(limitDoc);
        restaurantCollection.aggregate(Arrays.asList(unwind, filter, group, project,
                sort, skip, limit)).forEach(convertInRestaurant);

        return results;
    }


}
