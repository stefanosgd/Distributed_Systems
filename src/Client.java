import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

    private Client() {}

    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            // Get registry
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            // Lookup the remote object "Hello" from registry
            // and create a stub for it
            Scanner scan = new Scanner(System.in);

            FrontEndInterface stub1 = (FrontEndInterface) registry.lookup("Front_End");
            // Invoke a remote method
            System.out.println("Connected to Front-End");
            boolean connected = true;
            String movieTitle;
            int userID;
            double rating;
            while (connected) {
                System.out.println("What would you like to do?");
                System.out.println("(V)iew a rating?");
                System.out.println("(U)pdate a rating?");
                System.out.println("(A)dd a rating?");
                System.out.println("(Q)uit?");
                String userChoice = scan.nextLine();
                switch (userChoice.toUpperCase()) {
                    case "V":
                        System.out.println("Enter the title of the movie you want to get the rating for:");
                        movieTitle = scan.nextLine();
                        stub1.getMovieRating(movieTitle);
                        break;
                    case "U":
                        System.out.println("Enter the title of the movie you want to update the rating for:");
                        movieTitle = scan.nextLine();
                        System.out.println("Enter the user ID you want to update with");
                        userID = scan.nextInt();
                        System.out.println("Enter the new rating");
                        rating = scan.nextDouble();
                        stub1.updateMovieRating(movieTitle, userID, rating);
                        break;
                    case "A":
                        System.out.println("Enter the title of the movie you want to add the rating for");
                        movieTitle = scan.next();
                        System.out.println("Enter the user ID you want to add with");
                        userID = scan.nextInt();
                        System.out.println("Enter the new rating");
                        rating = scan.nextDouble();
                        stub1.submitMovieRating(movieTitle, userID, rating);
                        break;
                    case "Q":
                        connected = false;
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {//        System.out.println("Multiplied: " + response);
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
