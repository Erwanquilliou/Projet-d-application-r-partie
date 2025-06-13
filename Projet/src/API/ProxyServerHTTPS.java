package API;

import RMI.ServiceRestaurant;
import RMI.ServiceWaze;
import com.sun.net.httpserver.*;
import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class ProxyServerHTTPS {

    private static final int PORT = 8443;
    private static final String KEYSTORE_PATH = "keystore.jks";
    private static final String KEYSTORE_PASSWORD = "42MXH4PX87eY0SV";
    private static ServiceRestaurant restaurantService;
    private static ServiceWaze wazeService;

    public static void main(String[] args) throws Exception {
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

        char[] passphrase = KEYSTORE_PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            ks.load(fis, passphrase);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        HttpsServer server = HttpsServer.create(new InetSocketAddress(PORT), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    SSLContext c = getSSLContext();
                    SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    params.setSSLParameters(c.getDefaultSSLParameters());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Contexts API
        server.createContext("/api/waze-data", new WazeDataHandler());
        server.createContext("/api/restaurants", new RestaurantsHandler());
        server.createContext("/api/restaurant", new RestaurantHandler());
        server.createContext("/api/tables-libres", new TablesLibresHandler());
        server.createContext("/api/reserver", new ReservationHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur HTTPS démarré sur le port " + PORT);
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
            Date date = Date.valueOf(params[1].split("=")[1]);

            String response;
            try {
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

                int idRestaurant = Integer.parseInt(params.get("id"));
                int numTable = Integer.parseInt(params.get("numTable"));
                Date date = Date.valueOf(params.get("date"));
                String nom = params.get("nom");
                String prenom = params.get("prenom");
                String telephone = params.get("telephone");
                int nbPersonnes = Integer.parseInt(params.get("nbPersonnes"));

                try {
                    String response = restaurantService.reserverTable(
                            idRestaurant, numTable, date, nom, prenom, telephone, nbPersonnes
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