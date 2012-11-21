package system;

import java.rmi.RemoteException;

public class EntryMessage extends Message{
	private static final long serialVersionUID = 1L;
	private int ID;
	private Peer newPeer;
	EntryMessage(Peer newPeer, int ID){
		this.ID = ID;
		this.newPeer = newPeer;
	}
	
	public boolean evaluate(PeerImpl peer) {
		if (peer.networkSize == ID){
			return false;
		}
		
		
		peer.networkSize = ID;
		// If (int)log2(networkSize) > int(log2(networkSize-1)) : expand list by one element.
		
		
		
		// If new node is in between a connecting. Ask peer X for reference to X-1
		
		
		// how should the 1st peer get reference to prevPeer which is the new peer?
		if (peer.ID == 1){
			peer.prevPeer = newPeer;
		}
		for (int i = 0; i < peer.peers.length; i++){
			if ((peer.ID + i^2)%peer.networkSize < peer.ID ){
				if ((peer.ID +i^2) == ID){
					peer.peers[i] = newPeer;
				}else{
					try {
						peer.peers[i] =  peer.peers[i].getPrevPeer();
					} catch (RemoteException e) {
						System.out.println("ERROR: EntryMessage.evaluate()");
						e.printStackTrace();
					}
				}
			}
		}
	return true;
	}
}
