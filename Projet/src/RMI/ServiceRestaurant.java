package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.sql.Date;

public interface ServiceRestaurant extends Remote {
    String getRestaurants() throws RemoteException, RuntimeException;
    String getRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getMenuRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getTablesRestaurant(int indexRestaurant) throws RemoteException, RuntimeException;
    String getTablesLibreRestaurant(int indexRestaurant, Date date) throws RemoteException, RuntimeException;
    String reserverTable(String nom, String prenom, String telephone, String ticket) throws RemoteException, RuntimeException;
}
