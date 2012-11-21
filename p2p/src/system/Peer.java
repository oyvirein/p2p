package system;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Peer extends Remote {
	public void connect(Peer peer) throws RemoteException;
	public void accept(int ID, Peer[] peers, Peer prevPeer) throws RemoteException;
	public int getID() throws RemoteException;
	public void message(Message msg) throws RemoteException;
	public Peer getPrevPeer() throws RemoteException;
}
