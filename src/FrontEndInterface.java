import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FrontEndInterface extends Remote {
    void getMovieRating(String title) throws RemoteException;
    void submitMovieRating(String title, Integer userID, Double userRating) throws RemoteException;
    void updateMovieRating(String title, Integer userID, Double userRating) throws RemoteException;
}