import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class GossipServer implements GossipInterface {
    private HashMap<String, Integer> movies = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Double>> movieRatings = new HashMap<>();
    private int id;
    private GossipStatus status;
    private List<List<String>> logs = new ArrayList<>();
//    private static HashMap<GossipInterface, GossipStatus> gossipServers = new HashMap<>();
    private int timestamp;
    private HashMap<Integer, Integer> sharedTimestamp = new HashMap<>();
    // integer(?) own timestamp
    // list of other servers and the timestamp that they last spoke to each other


    public GossipServer(int ID) {
        this.id = ID;
        this.timestamp = 0;
        for (int i=1; i<4; i++) {
            if (i != this.id) {
                this.sharedTimestamp.put(i, this.timestamp);
            }
        }
        status = GossipStatus.ACTIVE;
        readData();
    }

    public GossipStatus getStatus() {
        return this.status;
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

    public List<List<String>> getLogsList(){
        return logs;
    }

    public Double getRatings(String title) {
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
            this.logs.add(new ArrayList<String>(Arrays.asList(Integer.toString(id), "GR-"+movieID, Long.toString(new Timestamp(System.currentTimeMillis()).getTime()))));
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
                return "Rating added";
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
                this.movieRatings.get(movieID).put(ID, rating);
                this.logs.add(new ArrayList<String>(Arrays.asList(Integer.toString(id), "SR-"+ Integer.toString(ID), Long.toString(new Timestamp(System.currentTimeMillis()).getTime()), Double.toString(rating))));
                return "Rating added";
            }
        }
        else {
            return "Rating not added as this movie does not exist!";
        }
    }


    public void gossip(){
//        try{
            // Randomly pick a server to gossip with (Provided they are online/overloaded)
            // Get the logs from the other servers
            // Join them together and order them according to timestamp
            // Make the changes required

//            int lOld = 0;
//            for (GossipInterface server : gossipServers.keySet()) {
//                List<List<String>> other = server.getLogsList();
//                int o = 0;
//                int l = 0;
//                List<List<String>> combLogs = new ArrayList<>();
//                while(true){
//                    //Need to check for both being equal to size
//                    if (l == logs.size() && o < other.size()){
//                        combLogs.addAll(other.subList(o, other.size()));
//                        logs = combLogs;
//                        break;
//                    } else if (o == other.size() && l < logs.size()) {
//                        combLogs.addAll(logs.subList(l, logs.size()));
//                        logs = combLogs;
//                        break;
//                    } else if (o < other.size() && l < logs.size()) {
//                        //attempt to interleave the logs on this server and others here to create joint dataset
//                        if ((new BigInteger(logs.get(l).get(2))).compareTo((new BigInteger(other.get(o).get(2)))) == 1){
//                            combLogs.add(other.get(o));
//                            o+=1;
//                        } else if ((new BigInteger(logs.get(l).get(2))).compareTo((new BigInteger(other.get(o).get(2)))) == -1){
//                            combLogs.add(logs.get(l));
//                            l+=1;
//                        } else {
//                            combLogs.add(logs.get(l));
//                            o+=1;
//                            l+=1;
//                        }
//                    } else {
//                        break;
//                    }
//
//                }
                System.out.println("Hello?");
                //smartCombine(movies, servers.get(i).getMoviesList());
//                logs = combLogs;
                //Now we have the logs in the right order need to edit data to fit the logs.
                //data being added repeatedly because same logs are being viewed again and again
//                for(int j = 0; j < logs.size(); j++){
//                    if(Integer.parseInt(logs.get(j).get(0)) != id && logs.get(j).get(1).contains("SR")){
//                        String movieIDGiven = logs.get(j).get(1).split("-")[1];
//                        String ratingGiven = logs.get(j).get(3);
//                        String ts = logs.get(j).get(2);
//                        //Check the list equality as below and this should work.
//                        if ((new ArrayList<>(Arrays.asList(movieIDGiven, ratingGiven, ts))).equals(movieRatings.get(ratings.size()-1))){
//                            continue;
//                        } else {
//                            ratings.add(new ArrayList<>(Arrays.asList(movieIDGiven, ratingGiven, ts)));
//                        }
//                    }
//                }

//            }
            //System.out.println(ratings.get(ratings.size()-1));
//        } catch(RemoteException e){
//            e.printStackTrace();
//        }
    }

    public static void main(String args[]) {
        try {
            // Create server object
            GossipServer GS1 = new GossipServer(1);
            GossipServer GS2 = new GossipServer(2);
            GossipServer GS3 = new GossipServer(3);

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
                    TimeUnit.SECONDS.sleep(5);
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
//                stub2.gossip();
//                stub3.gossip();
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
