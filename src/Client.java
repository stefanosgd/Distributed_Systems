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
            String userChoice;
            while (connected) {
                System.out.println("What would you like to do?");
                System.out.println("(V)iew a rating?");
                System.out.println("(U)pdate a rating?");
                System.out.println("(A)dd a rating?");
                System.out.println("(Q)uit?");
                userChoice = scan.nextLine();
                try {
                    switch (userChoice.toUpperCase()) {
                        case "V":
                            System.out.println("Enter the title of the movie you want to get the rating for:");
                            movieTitle = scan.nextLine();
                            System.out.println(stub1.getMovieRating(movieTitle));
                            break;
                        case "U":
                            System.out.println("Enter the title of the movie you want to update the rating for:");
                            movieTitle = scan.nextLine();
                            System.out.println("Enter your user ID:");
                            userID = scan.nextInt();
                            System.out.println("Enter the new rating");
                            rating = scan.nextDouble();
                            System.out.println(stub1.updateMovieRating(movieTitle, userID, rating));
                            userChoice = scan.nextLine();
                            break;
                        case "A":
                            System.out.println("Enter the title of the movie you want to add the rating for");
                            movieTitle = scan.nextLine();
                            System.out.println("Enter your user ID:");
                            userID = scan.nextInt();
                            System.out.println("Enter the new rating");
                            rating = scan.nextDouble();
                            System.out.println(stub1.submitMovieRating(movieTitle, userID, rating));
                            userChoice = scan.nextLine();
                            break;
                        case "Q":
                            connected = false;
                            break;
                        default:
                            break;
                    }
                }
                catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {//        System.out.println("Multiplied: " + response);
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
