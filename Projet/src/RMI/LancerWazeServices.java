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

public class LancerWazeServices {

    public static void main(String[] args) throws RemoteException {

        String proxyHost = "www-cache";
        boolean useProxy;
        int proxyPort = 3128;
        if(args.length!= 0 && args[0].equals("true")) {
            useProxy = true;
        }else{
            useProxy = false;
        }
        //pour executer sur une machine de l'iut, on met use_proxy à true.
        //pour lier le .jar à l'execution dans un terminal:
        //java -cp json-20210307.jar Waze.java
        //(bien garder le .java meme pour l'execution, fonctionne sans package je n'ai pas testé avec)

        int port = 1099;
        ServiceWaze serviceWaze = new Waze(proxyHost, proxyPort, useProxy);

        ServiceWaze servWaze = (ServiceWaze) UnicastRemoteObject.exportObject(serviceWaze, 0);

        //On récupère l'annuaire local du pc. Si il n'est pas lancé on le créer.
        Registry reg = LocateRegistry.getRegistry(port); //Pas de lien car c'est en local
        try {
            reg.rebind("waze", servWaze);
        } catch (ConnectException e) {
            reg = LocateRegistry.createRegistry(port);
            reg.rebind("waze", servWaze);
        }

    }
}
