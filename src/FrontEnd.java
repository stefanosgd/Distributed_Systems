import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.HashMap;

public class FrontEnd implements FrontEndInterface {

    private FrontEnd() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            // Lookup the remote object from registry
            GossipInterface stub1 = (GossipInterface) registry.lookup("Gossip1");
            GossipInterface stub2 = (GossipInterface) registry.lookup("Gossip2");
            GossipInterface stub3 = (GossipInterface) registry.lookup("Gossip3");
            this.gossipServers.put(stub1, stub1.getStatus());
            this.gossipServers.put(stub2, stub2.getStatus());
            this.gossipServers.put(stub3, stub3.getStatus());
            this.currentServer = stub1;
        } catch (Exception g) {
            System.err.println("Client exception: " + g.toString());
            g.printStackTrace();
        }
    }

    private HashMap<GossipInterface, GossipStatus> gossipServers = new HashMap<>();
    private GossipInterface currentServer;
    private int actionsTaken = 0;
    // Variable of current GossipServer index
    // If server is overloaded, temporarily choose a new GossipServer and then go back.
    // If the server goes offline, then you will choose a new constant GossipServer (modulo len)
    //

    public GossipInterface serverConnection() {
        try {
            for (GossipInterface server : this.gossipServers.keySet()) {
                gossipServers.put(server, server.getStatus());
            }
            // If current is online, use that
            // Else If overloaded try a new one temporarily
            // Else If offline, change current
            int attempts = 0;
            while (attempts < 4) {
                if (this.gossipServers.get(this.currentServer) == GossipStatus.ACTIVE) {
                    return this.currentServer;
                } else if (this.gossipServers.get(this.currentServer) == GossipStatus.OVERLOADED) {
                    for (GossipInterface server : this.gossipServers.keySet()) {
                        if (this.gossipServers.get(server) == GossipStatus.ACTIVE && server != this.currentServer && server.getLogsList().size() == actionsTaken) {
                            return server;
                        }
                    }
                } else {
                    for (GossipInterface server : this.gossipServers.keySet()) {
                        if (this.gossipServers.get(server) == GossipStatus.ACTIVE && server != this.currentServer && server.getLogsList().size() == actionsTaken) {
                            this.currentServer = server;
                            return this.currentServer;
                        }
                    }
                }
                attempts += 1;
            }
        } catch (Exception g) {
            System.err.println("Client exception: " + g.toString());
            g.printStackTrace();
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
                this.actionsTaken += 1;
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
                this.actionsTaken += 1;
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

//            // Lookup the remote object from registry
//            GossipInterface stub1 = (GossipInterface) registry.lookup("Gossip1");
//            GossipInterface stub2 = (GossipInterface) registry.lookup("Gossip2");
//            GossipInterface stub3 = (GossipInterface) registry.lookup("Gossip3");
//            gossipServers.put(stub1, GossipStatus.ACTIVE);
//            gossipServers.put(stub2, GossipStatus.ACTIVE);
//            gossipServers.put(stub3, GossipStatus.ACTIVE);
//            currentServer = stub1;
        } catch (Exception g) {
            System.err.println("Client exception: " + g.toString());
            g.printStackTrace();
        }
    }

}
