package RMI;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.RemoteException;
import java.time.Duration;

public class Waze implements ServiceWaze{
    private String proxyHost;
    private int proxyPort;
    private boolean useProxy;

    Waze(String proxyHost, int proxPort, boolean useProxy) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxPort;
        this.useProxy = useProxy;
    }

    @Override
    public String fetchWazeData() throws RemoteException, IOException, InterruptedException {
        try {
            String targetUrl = "https://carto.g-ny.org/data/cifs/cifs_waze_v2.json";

            HttpClient client;

            if (this.useProxy) {
                client = HttpClient.newBuilder()
                        .proxy(ProxySelector.of(new InetSocketAddress(this.proxyHost, this.proxyPort)))
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
            } else {
                client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("User-Agent", "Java HttpClient")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                return "{\"error\": \"Erreur HTTP: " + statusCode + "\"}";
            }

            String body = response.body();
            try {
                //tenter de parser le JSON pour vérifier qu'il est valide
                new JSONObject(body);
                return body; //retourner les données si elles sont valides
            } catch (Exception e) {
                return "{\"error\": \"Format de données invalide: " + e.getMessage() + "\"}";
            }

        } catch (IOException e) {
            return "{\"error\": \"Erreur réseau: " + e.getMessage() + "\"}";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{\"error\": \"Opération interrompue: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Erreur inattendue: " + e.getMessage() + "\"}";
        }
    }
}
