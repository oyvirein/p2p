package system;

public class GetReferenceMessage extends Message{
	private int targetID;
	
	GetReferenceMessage(int targetID){
		this.targetID = targetID;
	}
	private static final long serialVersionUID = 1L;

	public boolean evaluate(PeerImpl peer) {
		for (int i = 0; i < peer.peers.length; i++){
			int nextID = peer.ID + 2^i;
			
			if (nextID > targetID && targetID > peer.ID){
				this.send(peer.peers[i-1]);
				return false;
			}if (nextID > targetID && nextID < peer.ID){
				this.send(peer.peers[i-1]);
				return false;
			}
		}
		this.send(peer.peers[peer.peers.length-1]);
		
		return false;
	}

}
