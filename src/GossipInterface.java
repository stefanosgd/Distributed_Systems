import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GossipInterface extends Remote {
    Double getRatings(String title) throws RemoteException;
    String updateRatings(String title, Integer userID, Double movieRating) throws RemoteException;
    String submitRatings(String title, Integer userID, Double movieRating) throws RemoteException;
}
