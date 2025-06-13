package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.sql.Date;
import java.sql.Timestamp;

public interface ServiceRestaurant extends Remote {
    String getRestaurants() throws RemoteException, RuntimeException;
    String getRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getMenuRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getTablesRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getTablesLibreRestaurant(int indexRestaurant, Date date) throws RemoteException, RuntimeException;
    String reserverTable(int indexRestaurant, int numTable, Timestamp timestamp, String nom, String prenom, String telephone, int nbPersonne) throws RemoteException, RuntimeException;
}
