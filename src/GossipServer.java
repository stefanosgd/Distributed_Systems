import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class GossipServer implements GossipInterface {
    private HashMap<String, Integer> movies = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Double>> movieRatings = new HashMap<>();
    private int id;
    private GossipStatus status;
//    private List<List<String>> logs = new ArrayList<>();
    private ArrayList<String> logs = new ArrayList<>();
    private static HashMap<Integer, GossipInterface> gossipServers = new HashMap<>();
    private int timestamp;
    private HashMap<Integer, Integer> sharedTimestamp = new HashMap<>();
    // integer(?) own timestamp
    // list of other servers and the timestamp that they last spoke to each other


    public GossipServer(int ID) {
        this.id = ID;
        this.timestamp = 1;
//        this.logs.put(this.timestamp, "Started up");
//        timestamp += 1;
        for (int i=0; i<3; i++) {
            if (i != this.id) {
                this.sharedTimestamp.put(i, this.timestamp);
            }
        }
        status = GossipStatus.ACTIVE;
        gossipServers.put(this.id, this);
        readData();
    }

    public GossipStatus getStatus() {
        return this.status;
    }

    public void setStatus (GossipStatus newStatus) {
        this.status = newStatus;
    }

    public void readData(){
        String movieData;
        String ratingData;
        BufferedReader reader = null;
        String csvSearch = ",";
        try{
            reader = new BufferedReader(new FileReader("./movies.csv"));
            reader.readLine();
            while ((movieData = reader.readLine()) != null){
                int first = movieData.indexOf(csvSearch);
                int last = movieData.lastIndexOf(csvSearch);
                if (movieData.substring(last-1,last).equals("\"")) {
                    this.movies.put(movieData.substring(first+2, last-8), Integer.parseInt(movieData.substring(0, first)));
                }
                else if (movieData.substring(first, last).length() == 7) {
                    this.movies.put(movieData.substring(first+1, last), Integer.parseInt(movieData.substring(0, first)));
                }
                else {
                    this.movies.put(movieData.substring(first+1, last-7), Integer.parseInt(movieData.substring(0, first)));
                }
            }
            reader = new BufferedReader(new FileReader("./ratings.csv"));
            reader.readLine();
            while ((ratingData = reader.readLine()) != null){
                HashMap<Integer, Double> temp = new HashMap<>();
                String[] ratingList = ratingData.split(csvSearch);
                if (this.movieRatings.get(Integer.parseInt(ratingList[1])) != null) {
                    temp = this.movieRatings.get(Integer.parseInt(ratingList[1]));
                }
                temp.put(Integer.parseInt(ratingList[0]), Double.parseDouble(ratingList[2]));
                this.movieRatings.put(Integer.parseInt(ratingList[1]), temp);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<String> getLogsList(){
        return this.logs;
    }

    public Double getRatings(String title) {
        try {
            System.out.println(this.id);
            while (((this.sharedTimestamp.get((this.id + 1) % 3) != this.timestamp) && (gossipServers.get((this.id + 1) % 3).getStatus() != GossipStatus.OFFLINE)) ||
                   ((this.sharedTimestamp.get((this.id + 2) % 3) != this.timestamp) && (gossipServers.get((this.id + 2) % 3).getStatus() != GossipStatus.OFFLINE))) {
                // Waiting to gossip and update before querying
//                System.out.println("Waiting");
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
            return null;
        }
        // Check the list of other servers and make sure that it is up to date with f+1 of them
        // While timestamps don't match don't do anything
        if (this.movies.containsKey(title)) {
            int movieID = this.movies.get(title);
            double total = 0d;
            int count = 0;
            for (Double rating : this.movieRatings.get(movieID).values()) {
                total += rating;
                count += 1;
            }
//            this.timestamp += 1;
            return total/count;
        }
        else {
            return null;
        }
    }

    public String updateRatings(String title, Integer ID, Double rating) {
        if (this.movies.containsKey(title)) {
            int movieID = this.movies.get(title);
            if (this.movieRatings.get(movieID).containsKey(ID)) {
                this.movieRatings.get(movieID).put(ID, rating);
                this.logs.add(this.id + "," + this.timestamp+ "," + movieID + "," + ID+ ","+ rating);
                this.timestamp += 1;
//                this.logs.add(new ArrayList<String>(Arrays.asList(Integer.toString(this.id), ",", Integer.toString(this.timestamp), ",", "UR-"+ Integer.toString(ID), ",", Double.toString(rating))));
                return "Rating updated";
            }
            else {
                return "Rating not updated as this user has not yet submitted a rating for this movie! Try submit instead.";
            }
        }
        else {
            return "Rating not added as this movie does not exist!";
        }
    }

    public String submitRatings(String title, Integer ID, Double rating) {
        if (this.movies.containsKey(title)) {
            int movieID = this.movies.get(title);
            if (this.movieRatings.get(movieID).containsKey(ID)) {
                return "Rating not added as this user has already submitted a rating for this movie! Try update instead.";
            } else {
                this.movieRatings.get(movieID).putIfAbsent(ID, rating);
//                this.logs.add(new ArrayList<String>(Arrays.asList(Integer.toString(id), "SR-"+ Integer.toString(ID), Long.toString(new Timestamp(System.currentTimeMillis()).getTime()), Double.toString(rating))));
//                this.logs.put(this.timestamp, (this.id)+ ","+ (this.timestamp)+ ","+ "SR-"+ (ID)+ ","+ (rating));
                this.logs.add(this.id + "," + this.timestamp+ "," + movieID + "," + ID+ ","+ rating);
                this.timestamp += 1;
                return "Rating added";
            }
        }
        else {
            return "Rating not added as this movie does not exist!";
        }
    }

    public void exchangeGossip(GossipInterface otherServer, int otherID) {
        try {
                // Do the actions in the log file you got from the other server
            ArrayList<String> otherLog = otherServer.getLogsList();
            System.out.println(otherLog);
            for (String i : otherLog) {
                if (!this.logs.contains(i)) {
                    String[] logInstructions = i.split(",");
                    this.movieRatings.get(Integer.parseInt(logInstructions[2])).put(Integer.parseInt(logInstructions[3]), Double.parseDouble(logInstructions[4]));
                    this.logs.add(i);
                }
            }
            // Update when you last gossiped with this server
            this.sharedTimestamp.put(otherID, this.timestamp);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void gossip(){
        try{
            // Randomly pick a server to gossip with (Provided they are online/overloaded)
            // Get the logs from the other servers
            // Join them together and order them according to timestamp
            // Make the changes required
            int gossipID1 = (this.id+1) % 3;
            int gossipID2 = (this.id+2) % 3;
            if (Math.random() <= 0.5 && gossipServers.get(gossipID1).getStatus() != GossipStatus.OFFLINE) {
                exchangeGossip(gossipServers.get(gossipID1), gossipID1);
            }
            else if (gossipServers.get(gossipID2).getStatus() != GossipStatus.OFFLINE) {
                exchangeGossip(gossipServers.get(gossipID2), gossipID2);
            }
        } catch(RemoteException e){
            e.printStackTrace();
        }
    }


    public static void changingStatus(GossipInterface server) {
        try {
            if (server.getStatus() == GossipStatus.ACTIVE) {
                if (Math.random() < 0.1) {
                    server.setStatus(GossipStatus.OVERLOADED);
                }
                else if (Math.random() < 0.05) {
                    server.setStatus(GossipStatus.OFFLINE);
                }
            }
            else {
                if (Math.random() < 0.25) {
                    server.setStatus(GossipStatus.ACTIVE);
                }
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            // Create server object
            GossipServer GS1 = new GossipServer(0);
            GossipServer GS2 = new GossipServer(1);
            GossipServer GS3 = new GossipServer(2);

            // Create remote object stub from server object
            GossipInterface stub1 = (GossipInterface) UnicastRemoteObject.exportObject(GS1, 0);
            GossipInterface stub2 = (GossipInterface) UnicastRemoteObject.exportObject(GS2, 0);
            GossipInterface stub3 = (GossipInterface) UnicastRemoteObject.exportObject(GS3, 0);

            // Get registry
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);

            // Bind the remote object's stub in the registry
            registry.bind("Gossip1", stub1);
            registry.bind("Gossip2", stub2);
            registry.bind("Gossip3", stub3);

            // Write ready message to console
            System.err.println("Server ready");

            while(true){
                try{
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    System.out.println("exception :" + e.getMessage());
                }
                if (stub1.getStatus() != GossipStatus.OFFLINE) {
                    stub1.gossip();
                }
                if (stub2.getStatus() != GossipStatus.OFFLINE) {
                    stub2.gossip();
                }
                if (stub3.getStatus() != GossipStatus.OFFLINE) {
                    stub3.gossip();
                }
//                stub1.setStatus(GossipStatus.OFFLINE);
                changingStatus(stub1);
                changingStatus(stub2);
                changingStatus(stub3);
                for (int i : gossipServers.keySet()) {
                    System.out.println(gossipServers.get(i).getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
