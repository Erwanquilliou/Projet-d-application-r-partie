package API;

import RMI.ServiceRestaurant;
import RMI.ServiceWaze;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ProxyServer {

    private static final int PORT = 8080;
    private static ServiceRestaurant restaurantService;
    private static ServiceWaze wazeService;

    public static void main(String[] args) throws IOException {
        try {
            Registry registry1 = LocateRegistry.getRegistry(args[0], 1099);
            restaurantService = (ServiceRestaurant) registry1.lookup("restaurant");

            Registry registry2 = LocateRegistry.getRegistry(args[1], 1099);
            wazeService = (ServiceWaze) registry2.lookup("waze");

            System.out.println("Connexion au service RMI réussie");
        } catch (Exception e) {
            System.err.println("Erreur de connexion au service RMI: " + e.getMessage());
            e.printStackTrace();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/waze-data", new WazeDataHandler());
        server.createContext("/api/restaurants", new RestaurantsHandler());
        server.createContext("/api/restaurant", new RestaurantHandler());
        server.createContext("/api/tables-libres", new TablesLibresHandler());
        server.createContext("/api/reserver", new ReservationHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur démarré sur le port " + PORT);
    }

    static class WazeDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            //configuration CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                System.out.println("Récupération des données Waze...");
                String response = wazeService.fetchWazeData();
                System.out.println("Données Waze récupérées, longueur: " + response.length());

                byte[] responseBytes = response.getBytes("UTF-8");

                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                    os.flush();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement des données Waze: " + e.getMessage());
                e.printStackTrace();

                String errorResponse = "{\"error\":\"Erreur serveur: " + e.getMessage().replace("\"", "'") + "\"}";
                byte[] errorBytes = errorResponse.getBytes("UTF-8");

                exchange.sendResponseHeaders(500, errorBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorBytes);
                }
            }
        }
    }

    static class RestaurantsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            String response;
            try {
                response = restaurantService.getRestaurants();
            } catch (Exception e) {
                response = "{\"error\": \"" + e.getMessage() + "\"}";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class RestaurantHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            String query = exchange.getRequestURI().getQuery();
            int idRestaurant = Integer.parseInt(query.split("=")[1]);

            String response;
            try {
                response = restaurantService.getRestaurant(idRestaurant);
            } catch (Exception e) {
                response = "{\"error\": \"" + e.getMessage() + "\"}";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class TablesLibresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            int idRestaurant = Integer.parseInt(params[0].split("=")[1]);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateStr = params[1].split("=")[1];
            String timeStr = params[2].split("=")[1];

            String response;
            try {
                java.util.Date parsedDate = dateFormat.parse(dateStr + " " + timeStr);
                Timestamp date = new Timestamp(parsedDate.getTime());
                response = restaurantService.getTablesLibreRestaurant(idRestaurant, date);
            } catch (Exception e) {
                response = "{\"error\": \"" + e.getMessage() + "\"}";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class ReservationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                if (query == null) {
                    throw new IllegalArgumentException("Paramètres manquants");
                }

                Map<String, String> params = new HashMap<>();
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = java.net.URLDecoder.decode(pair[1], "UTF-8");
                        params.put(key, value);
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                int idRestaurant = Integer.parseInt(params.get("id"));
                int numTable = Integer.parseInt(params.get("numTable"));
                java.util.Date date = dateFormat.parse(params.get("date") + " " + params.get("time"));
                Timestamp timestamp = new Timestamp(date.getTime());
                String nom = params.get("nom");
                String prenom = params.get("prenom");
                String telephone = params.get("telephone");
                int nbPersonnes = Integer.parseInt(params.get("nbPersonnes"));

                try {
                    String response = restaurantService.reserverTable(
                            idRestaurant, numTable, timestamp, nom, prenom, telephone, nbPersonnes
                    );

                    try {
                        if (!response.startsWith("{") || !response.endsWith("}")) {
                            response = "{\"status\":\"error\",\"message\":\"Réponse invalide du serveur\"}";
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la validation du JSON : " + e.getMessage());
                        response = "{\"status\":\"error\",\"message\":\"Erreur de format JSON\"}";
                    }

                    byte[] responseBytes = response.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Exception inconnue";
                    } else {
                        errorMessage = errorMessage
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                    }

                    String errorResponse = "{\"status\":\"error\",\"message\":\"" + errorMessage + "\"}";
                    byte[] responseBytes = errorResponse.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = "{\"status\":\"error\",\"message\":\"Erreur de traitement de la requête\"}";
                byte[] responseBytes = errorResponse.getBytes("UTF-8");
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        }
    }
}