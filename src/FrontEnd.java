import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class FrontEnd implements FrontEndInterface {

    private FrontEnd() {
    }

    private static HashMap<GossipInterface, GossipStatus> gossipServers = new HashMap<>();

    public void getMovieRating(String title) {
        for (GossipInterface server : gossipServers.keySet()) {
            if (gossipServers.get(server) == GossipStatus.ACTIVE) {
                try {
                    Double response = server.getRatings(title);
                    System.out.println("Rating: " + response);
                } catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void submitMovieRating(String title, Integer userID, Double userRating) {
        for (GossipInterface server : gossipServers.keySet()) {
            if (gossipServers.get(server) == GossipStatus.ACTIVE) {
                try {
                    System.out.println(server.submitRatings(title, userID, userRating));
                } catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void updateMovieRating(String title, Integer userID, Double userRating) {
        for (GossipInterface server : gossipServers.keySet()) {
            if (gossipServers.get(server) == GossipStatus.ACTIVE) {
                try {
                    System.out.println(server.updateRatings(title, userID, userRating));
                } catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static void main(String[] args) {

        try {
            // Create server object
            FrontEnd obj = new FrontEnd();
            // Create remote object stub from server object
            FrontEndInterface stub = (FrontEndInterface) UnicastRemoteObject.exportObject(obj, 0);
            // Get registry
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            // Bind the remote object's stub in the registry
            registry.bind("Front_End", stub);
            // Write ready message to console
            System.err.println("Server ready");

            // Lookup the remote object from registry
            GossipInterface stub1 = (GossipInterface) registry.lookup("Gossip1");
            GossipInterface stub2 = (GossipInterface) registry.lookup("Gossip2");
            GossipInterface stub3 = (GossipInterface) registry.lookup("Gossip3");
            gossipServers.put(stub1, GossipStatus.ACTIVE);
            gossipServers.put(stub2, GossipStatus.ACTIVE);
            gossipServers.put(stub3, GossipStatus.ACTIVE);
        } catch (Exception g) {
            System.err.println("Client exception: " + g.toString());
            g.printStackTrace();
        }
    }

}
