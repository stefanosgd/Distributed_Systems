import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FrontEndInterface extends Remote {
    GossipInterface serverConnection() throws RemoteException;
    String getMovieRating(String title) throws RemoteException;
    String submitMovieRating(String title, Integer userID, Double userRating) throws RemoteException;
    String updateMovieRating(String title, Integer userID, Double userRating) throws RemoteException;
}