import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.HashMap;

public class FrontEnd implements FrontEndInterface {

    private FrontEnd() {
    }

    private static HashMap<GossipInterface, GossipStatus> gossipServers = new HashMap<>();
    private static GossipInterface currentServer;
    // Variable of current GossipServer index
    // If server is overloaded, temporarily choose a new GossipServer and then go back.
    // If the server goes offline, then you will choose a new constant GossipServer (modulo len)
    //

    public GossipInterface serverConnection() {
        // If current is online, use that
        // Else If overloaded try a new one temp
        // Else If offline, change current
        int attempts = 0;
        while (attempts < 4) {
            if (gossipServers.get(currentServer) == GossipStatus.ACTIVE) {
                return currentServer;
            }
            else if (gossipServers.get(currentServer) == GossipStatus.OVERLOADED) {
                for (GossipInterface server : gossipServers.keySet()) {
                    if (gossipServers.get(server) == GossipStatus.ACTIVE && server != currentServer) {
                        return server;
                    }
                }
            }
            else {
                for (GossipInterface server : gossipServers.keySet()) {
                    if (gossipServers.get(server) == GossipStatus.ACTIVE && server != currentServer) {
                        currentServer = server;
                        return currentServer;
                    }
                }
            }
            attempts += 1;
        }
        return null;
    }

    public String getMovieRating(String title) {
        GossipInterface using = serverConnection();
        if (using == null) {
            return "There was an error connecting to the server";
        } else {
            try {
                Double response = using.getRatings(title);
                if (response != null) {
                    String formattedResponse = new DecimalFormat("#.00").format(response);
                    return "Rating: " + formattedResponse;
                } else {
                    return "This movie does not exist in the system.";
                }
            } catch (Exception e) {
                return "Client exception: " + e.toString();
//                e.printStackTrace();
            }
        }
    }

    public String submitMovieRating(String title, Integer userID, Double userRating) {
        GossipInterface using = serverConnection();
        if (using == null) {
            return "There was an error connecting to the server";
        } else {
            try {
                return using.submitRatings(title, userID, userRating);
            } catch (Exception e) {
                return "Client exception: " + e.toString();
//                e.printStackTrace();
            }
        }
    }

    public String updateMovieRating(String title, Integer userID, Double userRating) {
        GossipInterface using = serverConnection();
        if (using == null) {
            return "There was an error connecting to the server";
        } else {
            try {
                return using.updateRatings(title, userID, userRating);
            } catch (Exception e) {
                return "Client exception: " + e.toString();
//                        e.printStackTrace();
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
            currentServer = stub1;
        } catch (Exception g) {
            System.err.println("Client exception: " + g.toString());
            g.printStackTrace();
        }
    }

}
