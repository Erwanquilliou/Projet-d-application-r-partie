package RMI;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.Properties;

public class LancerServices {

    public static void main(String[] args) throws RemoteException {

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("conf.ini")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier de configuration", e);
        }
        String login = props.getProperty("login");
        String mdp = props.getProperty("mdp");

        Connexion connexion = Connexion.getInstance();
        connexion.connect(login, mdp);

        int port = 1099;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        ServiceRestaurant serviceResto = new Restaurant();

        ServiceRestaurant serviceRestaurant = (ServiceRestaurant) UnicastRemoteObject.exportObject(serviceResto, 0);

        //On récupère l'annuaire local du pc. Si il n'est pas lancé on le créer.
        Registry reg = LocateRegistry.getRegistry(port); //Pas de lien car c'est en local
        try {
            reg.rebind("restaurant", serviceRestaurant);
        } catch (ConnectException e) {
            reg = LocateRegistry.createRegistry(port);
            reg.rebind("restaurant", serviceRestaurant);
        }

    }
}
