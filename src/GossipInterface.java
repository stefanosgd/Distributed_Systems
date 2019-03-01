import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GossipInterface extends Remote {
    void readData() throws RemoteException;
    GossipStatus getStatus() throws RemoteException;
    Double getRatings(String title) throws RemoteException;
    String updateRatings(String title, Integer userID, Double movieRating) throws RemoteException;
    String submitRatings(String title, Integer userID, Double movieRating) throws RemoteException;
    List<List<String>> getLogsList() throws RemoteException;
    void gossip() throws RemoteException;
}
