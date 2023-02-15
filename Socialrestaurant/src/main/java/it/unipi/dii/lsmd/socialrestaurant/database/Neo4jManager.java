package it.unipi.dii.lsmd.socialrestaurant.database;

import it.unipi.dii.lsmd.socialrestaurant.model.Restaurant;
import it.unipi.dii.lsmd.socialrestaurant.model.User;
import javafx.util.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;
public class Neo4jManager {

    Driver driver;

    public Neo4jManager(Driver driver) {
        this.driver = driver;
    }

    /**
     * Function that add the info of a new user to GraphDB
     * @param u new User
     */
    public boolean addUser(User u) {
        boolean res = false;
        try(Session session = driver.session()) {
            res = session.writeTransaction((TransactionWork<Boolean>) tx -> {
                tx.run("CREATE (u:User {username: $username})",
                        parameters("username", u.getUsername()));

                return true;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return res;
    }

    public boolean addRestaurant(Restaurant r) {
        boolean res = false;
        try(Session session = driver.session()) {
            res = session.writeTransaction((TransactionWork<Boolean>) tx -> {
                tx.run("CREATE (r:Restaurant{name: $name, city: $city, cuisine: $cuisine})",
                        parameters("name", r.getName(),"city", r.getCity(),"cuisine", r.getCuisine()));

                return true;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return res;
    }
    /**
     * Function that deletes a User from the GraphDB
     * @param u User to delete
     * @return True if the operation is done successfully, false otherwise
     */
    public boolean deleteUser(User u) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.username = $username DETACH DELETE u",
                        parameters("username", u.getUsername()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(String oldUsername, String username) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $oldUsername}) SET u.username = $username",
                        parameters("username", username, "oldUsername", oldUsername));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRestaurant(Restaurant r) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (r:Restaurant) WHERE r.name = $name DETACH DELETE r",
                        parameters("name", r.getName()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * return the number of follower of the user
     * @param username username of the user
     * @return number of followers
     */
    public int getNumFollowersUser(final String username) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})<-[r:FOLLOWS]-() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("username", username));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }

    /**
     * return the number of follower of the user
     * @param username username of the user
     * @return number of followers
     */
    public int getNumFollowingUser(final String username) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[r:FOLLOWS]->() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("username", username));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }

    public boolean userAFollowsUserB (String userA, String userB) {
        boolean res = false;
        try(Session session = driver.session()) {
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{username:$userA})-[r:FOLLOWS]->(b:User{username:$userB}) " +
                        "RETURN COUNT(*)", parameters("userA", userA, "userB", userB));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Add a FOLLOWS relationship between two Users
     * @param username follower
     * @param target followed
     */
    public void followUser (final String username, final String target,String timestamp) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $username}), (t:User {username: $target}) " +
                                "MERGE (u)-[p:FOLLOWS]->(t) " +
                                "ON CREATE SET p.date = $timestamp",
                        parameters("username", username, "target", target,"timestamp",timestamp));
                return null;
            });
        }
    }

    /**
     * remove the relationship of FOLLOWS between two Users
     * @param target followed
     * @param username follower
     */
    public void unfollowUser (final String username, final String target) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {username: $username})-[r:FOLLOWS]->(:User {username: $target}) " +
                                "DELETE r",
                        parameters("username", username, "target", target));
                return null;
            });
        }
    }

    /**
     * Function that adds a Like relationship between a User and a Restaurant
     * @param u User
     * @param r Restaurant
     */
    public void like(User u, Restaurant r, String timestamp) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User), (b:Restaurant) " +
                                "WHERE a.username = $username AND (b.name = $name) " +
                                "MERGE (a)-[r:LIKE]->(b)" +
                                "ON CREATE SET r.date = $timestamp",
                        parameters("username", u.getUsername(),
                                "name", r.getName(),"timestamp",timestamp));
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean unlike(User u, Restaurant r) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User{username:$username})-[r:LIKE]->(b:Restaurant) " +
                                "WHERE b.name = $name" +
                                " DELETE r",
                        parameters("username", u.getUsername(),
                                "name", r.getName()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that adds an Add relationship between a User and a Restaurant
     * @param u User
     * @param r Restaurant
     */
    public void add(User u, Restaurant r) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User), (b:Restaurant) " +
                                "WHERE a.username = $username AND (b.name = $name) " +
                                "MERGE (a)-[r:ADD]->(b)",
                        parameters("username", u.getUsername(),
                                "name", r.getName()));
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean remove(User u, Restaurant r) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User{username:$username})-[r:ADD]->(b:Restaurant) " +
                                "WHERE b.name = $name" +
                                " DELETE r",
                        parameters("username", u.getUsername(),
                                "name", r.getName()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * Function that return true if exist a relation user-add->restaurant
     * @param user Username
     * @param restaurant Restaurant object
     */
    public boolean userAddRestaurant (String user, Restaurant restaurant) {
        boolean res = false;
        try (Session session = driver.session()) {
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (:User{username:$user})-[r:ADD]->(p:Restaurant) WHERE (p.name = $name) " +
                        "RETURN COUNT(*)", parameters("user", user, "name", restaurant.getName()));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Function that return true if exist a relation user-like->restaurant
     * @param user Username
     * @param restaurant Restaurant object
     */
    public boolean userLikeRestaurant (String user, Restaurant restaurant){
        boolean res = false;
        try(Session session = driver.session()){
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (:User{username:$user})-[r:LIKE]->(p:Restaurant) WHERE (p.name = $name) " +
                        "RETURN COUNT(*)", parameters("user", user, "name", restaurant.getName()));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }


    /**
     * return the number of likes of a restaurant
     * @param restaurant Restaurant object
     * @return number of likes
     */
    public int getNumLikes(final Restaurant restaurant) {
        int numLikes;
        try (Session session = driver.session()) {
            numLikes = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (p:Restaurant)<-[r:LIKE]-() WHERE p.name = $name " +
                        "RETURN COUNT(r) AS numLikes", parameters("name", restaurant.getName()));
                return result.next().get("numLikes").asInt();
            });
        }
        return numLikes;
    }

    /**
     *
     * @param u Logged User
     * @param limit
     * @param skip
     * @return
     */
    public List<User> getSnapsOfFollowedUserByKeyword (User u, String keyword, int limit, int skip) {
        List<User> followedUsers;
        try (Session session = driver.session()) {
            followedUsers = session.writeTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[:FOLLOWS]->(u:User) " +
                                "WHERE toLower(u.username) CONTAINS $keyword " +
                                "RETURN u.username AS Username ORDER BY Username DESC " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username", u.getUsername(), "keyword", keyword, "limit", limit, "skip", skip));
                List<User> followedList = new ArrayList<>();
                while(result.hasNext()) {
                    Record record = result.next();
                    User snap = new User(null,
                            null,null, record.get("Username").asString(),null,-1, null, -1);
                    followedList.add(snap);
                }
                return followedList;
            });
        }
        System.out.println(followedUsers);
        return followedUsers;
    }

    /**
     * Function that returns a list of suggested users snapshots for the logged user.
     * Suggestions are based on most followed users who are 2 FOLLOWS hops far from the
     * logged user (first level);
     * The second level of suggestion returns most followed users that have likes in common with
     * the logged user.
     *
     * @param u user who need suggestions
     * @param numberFirstLv how many users suggest from first level suggestion
     * @param numberSecondLv how many users suggest from second level
     * @return A list of suggested users snapshots
     */
    public List<User> getSnapsOfSuggestedUsers(User u, int numberFirstLv, int numberSecondLv, int skipFirstLv, int skipSecondLv) {
        List<User> usersSnap = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (me:User {username: $username})-[:FOLLOWS*2..2]->(target:User), " +
                                "(target)<-[r:FOLLOWS]-() " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target)) " +
                                "RETURN DISTINCT target.username AS Username, " +
                                "COUNT(DISTINCT r) as numFollower " +
                                "ORDER BY numFollower DESC, Username " +
                                "SKIP $skipFirstLevel " +
                                "LIMIT $firstLevel " +
                                "UNION " +
                                "MATCH (me:User {username: $username})-[:LIKE]->()<-[:LIKE]-(target:User), " +
                                "(target)<-[r:FOLLOWS]-() " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target)) " +
                                "RETURN target.username AS Username, " +
                                "COUNT(DISTINCT r) as numFollower " +
                                "ORDER BY numFollower DESC, Username " +
                                "SKIP $skipSecondLevel " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstLevel", numberFirstLv, "secondLevel", numberSecondLv,  "skipFirstLevel", skipFirstLv, "skipSecondLevel", skipSecondLv));
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), "",
                            "","","",-1, new ArrayList<>(), 0);

                    usersSnap.add(snap);
                }
                return null;
            });
        }
        return usersSnap;
    }

    /**
     * Function that returns a list of suggested restaurants snapshots for the logged user.
     * Suggestions are based on restaurants liked by followed users (first level) and restaurants liked by users
     * that are 2 FOLLOWS hops far from the logged user (second level).
     * Restaurants returned are ordered by the number of times they appeared in the results, so restaurants
     * that appear more are most likely to be similar to the interests of the logged user.
     *
     * @param u Logged User
     * @param numberFirstLv how many restaurants suggest from first level
     * @param numberSecondLv how many restaurants suggest from second level
     * @return A list of suggested restaurants snapshots
     */
    public List<Restaurant> getSnapsOfSuggestedRestaurants(User u, int numberFirstLv, int numberSecondLv, int skipFirstLv, int skipSecondLv) {
        List<Restaurant> restaurantsSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:Restaurant)<-[r:LIKE]-(u:User)<-[:FOLLOWS]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKE]->(target)) " +
                                "RETURN target.name AS Name, target.city AS City, target.cuisine as Cuisine, " +
                                "COUNT(*) AS nOccurences " +
                                "ORDER BY nOccurences DESC, Name " +
                                "SKIP $skipFirstLevel " +
                                "LIMIT $firstlevel " +
                                "UNION " +
                                "MATCH (target:Restaurant)<-[r:LIKE]-(u:User)<-[:FOLLOWS*2..2]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKE]->(target)) " +
                                "RETURN target.name AS Name, target.city AS City, target.cuisine as Cuisine, " +
                                "COUNT(*) AS nOccurences " +
                                "ORDER BY nOccurences DESC, Name " +
                                "SKIP $skipSecondLevel " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstlevel", numberFirstLv, "secondLevel", numberSecondLv, "skipFirstLevel", skipFirstLv, "skipSecondLevel", skipSecondLv));
                while(result.hasNext()){
                    Record r = result.next();
                    Restaurant snap = new Restaurant( r.get("Name").asString(),
                            r.get("City").asString(),
                            r.get("Cuisine").asString(),
                            new ArrayList<>());

                    restaurantsSnap.add(snap);
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return restaurantsSnap;
    }

    /**
     * Method that returns categories with the highest number of likes in the specified period of time.
     * @param period
     * @return list of categories and the number of likes
     */
    public List<Pair<String, Integer>> getCategoriesSummaryByLikes(String period) {
        List<Pair<String, Integer>> results = new ArrayList<>();
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

        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (p:Restaurant)<-[l:LIKE]-(:User) " +
                                "WHERE l.date >= $start_date " +
                                "RETURN COUNT(l) AS nLikes, p.cuisine AS Cuisine " +
                                "ORDER BY nLikes DESC",
                        parameters( "start_date", filterDate));

                while(result.hasNext()){
                    Record r = result.next();
                    results.add(new Pair(r.get("Cuisine").asString(), r.get("nLikes").asInt()));
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return results;
    }


    /**
     * Method that returns restaurants with the highest number of likes in the specified period of time.
     * @param limit
     * @return List of Restaurants
     */
    public List<Pair<Restaurant, Integer>> getMostLikedRestaurants(String period, int skip, int limit) {
        List<Pair<Restaurant, Integer>> topRestaurants = new ArrayList<>();
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
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println(startOfDay);
        System.out.println(filterDate);
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[l:LIKE]->(p:Restaurant) " +
                                "WHERE l.date >= $start_date " +
                                "RETURN p.name AS Name, p.city AS City, p.cuisine AS Cuisine, " +
                                "COUNT(l) AS like_count " +
                                "ORDER BY like_count DESC, Name " +
                                "SKIP $skip " +
                                "LIMIT $limit",
                        parameters( "start_date", filterDate,"skip", skip, "limit", limit));

                while(result.hasNext()){
                    Record r = result.next();

                    Restaurant snap = new Restaurant( r.get("Name").asString(),
                            r.get("City").asString(),
                            r.get("Cuisine").asString(),
                            new ArrayList<>());

                    topRestaurants.add(new Pair(snap, r.get("like_count").asInt()));
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return topRestaurants;
    }

    /**
     * Return a hashmap with the most popular user
     * @param num num of rank
     * @return pair (name, numFollower)
     */
    public List<Pair<User, Integer>> getMostFollowedUsers (int skip, int num) {
        List<Pair<User, Integer>> rank;
        try (Session session = driver.session()) {
            rank = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:User)<-[r:FOLLOWS]-(:User) " +
                                "RETURN DISTINCT target.username AS Username, " +
                                "COUNT(DISTINCT r) as numFollower " +
                                "ORDER BY numFollower DESC, Username " +
                                "SKIP $skip " +
                                "LIMIT $num",
                        parameters("skip", skip, "num", num));
                List<Pair<User, Integer>> popularUser = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), "", "","","",-1, new ArrayList<>(), 0);

                    popularUser.add(new Pair(snap, r.get("numFollower").asInt()));
                }
                return popularUser;
            });
        }
        return rank;
    }
}





