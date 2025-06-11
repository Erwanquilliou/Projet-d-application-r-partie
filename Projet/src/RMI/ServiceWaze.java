package RMI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceWaze extends Remote {
    String fetchWazeData() throws RemoteException, IOException, InterruptedException;
}
