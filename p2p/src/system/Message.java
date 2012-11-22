package system;

import java.io.Serializable;
import java.rmi.RemoteException;

public abstract class Message implements Cloneable, Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void action(PeerImpl peer); // Input: this. Responds to message. Can forward message with send() and broadcast() with broadcast.
	
	public void broadcast(Peer[] peers){
		if (peers == null){
			return;
			
		}else{
			for (int i = 0; i < peers.length; i++){
				try {
					peers[i].message((Message)this.clone());
				} catch (RemoteException e) {
					System.out.println("ERROR Message: unable to broadcast ");
					e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					System.out.println("ERROR Message: unable to clone message");
					e.printStackTrace();
				}
			}
		}
	};
	public void send(Peer peer){
		try {
			peer.message((Message)this.clone());
		} catch (RemoteException e) {
			System.out.println("ERROR Message: unable to send message");
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			System.out.println("ERROR Message: unable to clone message");
			e.printStackTrace();
		}
	}
}
