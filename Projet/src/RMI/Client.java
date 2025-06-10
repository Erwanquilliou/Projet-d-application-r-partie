package RMI;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

    private static final String SERVICE_NAME = "restaurant";

    public static void main(String[] args) {
        ServiceRestaurant restaurantService = null;
        Scanner scanner = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // 2. Look up the remote object using its registered name
            restaurantService = (ServiceRestaurant) registry.lookup(SERVICE_NAME);
            System.out.println("Successfully looked up the RestaurantService: " + SERVICE_NAME);

            // --- Test 1: Get all Nancy restaurant coordinates ---
            System.out.println("\n--- Fetching Nancy Restaurant Coordinates ---");
            String restaurantsJson = restaurantService.getRestaurants();
            System.out.println("Received Restaurant Data (JSON):\n" + restaurantsJson);
            System.out.println("----------------------------------------------");

            // --- Test 2: Make a reservation ---
            System.out.println("\n--- Reservation ---");
            System.out.println("Veuillez renseigner les informations de la réservation:");

            System.out.print("ID Restaurant : ");
            int idrest = scanner.nextInt();

            System.out.print("Numéro table : ");
            int numtab = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over

            System.out.print("Date de reservation (YYYY-MM-DD): ");
            String dateStr = scanner.nextLine();
            Date datres = Date.valueOf(dateStr);

            System.out.print("Nombre de personnes : ");
            int nbpers = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Ton nom: ");
            String nom = scanner.nextLine();

            System.out.print("Ton prénom: ");
            String prenom = scanner.nextLine();

            System.out.print("ton numéro de téléphone: ");
            String telephone = scanner.nextLine();

            String reservationResult = restaurantService.reserverTable(idrest, numtab, datres, nom, prenom, telephone, nbpers);
            System.out.println("Reservation Result (JSON):\n" + reservationResult);
            System.out.println("----------------------------------------------");


            System.out.println("\n--- Attempting a Duplicate Reservation (expected to fail) ---");
            String duplicateReservationResult = restaurantService.reserverTable(
                    idrest, numtab, datres, "DuplicateGuest", "Duplicate", "0000000000", 2
            );
            System.out.println("Duplicate Reservation Result (JSON):\n" + duplicateReservationResult);
            System.out.println("----------------------------------------------");


        } catch (java.rmi.ConnectException e) {
            System.err.println("Connection error: Could not connect to RMI Registry.");
            System.err.println("Please ensure the RMI Registry and RestaurantServer are running.");
            System.err.println("Error details: " + e.getMessage());
        } catch (java.rmi.NotBoundException e) {
            System.err.println("Service not found in RMI Registry.");
            System.err.println("Please ensure the '" + SERVICE_NAME + "' is correctly bound by the server.");
            System.err.println("Error details: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Format de date invalide. Utilisez le format YYYY-MM-DD.");
            System.err.println("Error details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during RMI client operation:");
            e.printStackTrace();
        } finally {
            scanner.close(); // Close the scanner
        }
    }
}