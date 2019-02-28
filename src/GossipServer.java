import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class GossipServer implements GossipInterface {
    static private HashMap<String, Integer> movies = new HashMap<>();
    static private HashMap<Integer, HashMap<Integer, Double>> movieRatings = new HashMap<>();
    private int id;
    private GossipStatus status;

    public GossipServer(int ID) {
        this.id = ID;
        status = GossipStatus.ACTIVE;
        readData();
    }

    private static void readData(){
        String movieData;
        String ratingData;
        BufferedReader reader = null;
        String csvSplit = ",";
        try{
            reader = new BufferedReader(new FileReader("./movies.csv"));
            reader.readLine();
            while ((movieData = reader.readLine()) != null){
                String[] movieList = movieData.split(csvSplit);
                movies.put(movieList[1], Integer.parseInt(movieList[0]));
            }
            reader = new BufferedReader(new FileReader("./ratings.csv"));
            reader.readLine();
            while ((ratingData = reader.readLine()) != null){
                HashMap<Integer, Double> temp = new HashMap<>();
                String[] ratingList = ratingData.split(csvSplit);
                if (movieRatings.get(Integer.parseInt(ratingList[1])) != null) {
                    temp = movieRatings.get(Integer.parseInt(ratingList[1]));
                }
                temp.put(Integer.parseInt(ratingList[0]), Double.parseDouble(ratingList[2]));
                movieRatings.put(Integer.parseInt(ratingList[1]), temp);
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

    public Double getRatings(String title) {
        if (movies.containsKey(title)) {
            int movieID = movies.get(title);
            double total = 0d;
            int count = 0;
            for (Double rating : movieRatings.get(movieID).values()) {
                total += rating;
                count += 1;
            }
            return total/count;
        }
        else {
            return null;
        }
    }

    public String updateRatings(String title, Integer ID, Double rating) {
        if (movies.containsKey(title)) {
            int movieID = movies.get(title);
            if (movieRatings.get(movieID).containsKey(ID)) {
                movieRatings.get(movieID).put(ID, rating);
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
        if (movies.containsKey(title)) {
            int movieID = movies.get(title);
            if (movieRatings.get(movieID).containsKey(ID)) {
                return "Rating not added as this user has already submitted a rating for this movie! Try update instead.";
            } else {
                movieRatings.get(movieID).put(ID, rating);
                return "Rating added";
            }
        }
        else {
            return "Rating not added as this movie does not exist!";
        }
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
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
