import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GossipInterface extends Remote {
    void readData() throws RemoteException;
    void setStatus(GossipStatus newStatus) throws RemoteException;
    GossipStatus getStatus() throws RemoteException;
    Double getRatings(String title) throws RemoteException;
    String updateRatings(String title, Integer userID, Double movieRating) throws RemoteException;
    String submitRatings(String title, Integer userID, Double movieRating) throws RemoteException;
    ArrayList getLogsList() throws RemoteException;
    void gossip() throws RemoteException;
    void exchangeGossip(GossipInterface otherServer, int otherID) throws RemoteException;
}
