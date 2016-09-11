import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Game24_Backend extends Remote{
	boolean login(String name,char[] password) throws RemoteException;
	boolean register(String name, char[] password) throws RemoteException;
	boolean logout(String name) throws RemoteException;
	float[] getinfo(String name) throws RemoteException;
	String[] getLeaderBoard() throws RemoteException;

}
