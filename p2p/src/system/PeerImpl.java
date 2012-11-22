package system;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;



public class PeerImpl implements Peer {
	static PeerImpl peer;
	public int networkSize;
	public Peer [] peers;
	public int ID = 0;
	public Peer prevPeer;
	
	static final BlockingDeque<Message> messages = new LinkedBlockingDeque<Message>();
	public static void main(String[] args) {
		if (args.length == 0){
			System.out.println("Missing initPeer argument");
			return;
		}
		
		try {
			if (System.getSecurityManager() == null ) { 
				System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
			}
			
			// ------------------------------------ Starting RMI server. Portnumber is arg[0] -----------------------
			peer = new PeerImpl();
			Peer stub = (Peer)UnicastRemoteObject.exportObject((Peer)peer, 0);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("Peer", stub);
			
			//-------------------------------------- Connecting to initPeer. Address is args[1], port is args[2] --------------------------------
			if (args.length > 2){
				Registry remoteRegistry = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));
	    		Peer initPeer  = (Peer) remoteRegistry.lookup("Peer");
	    		initPeer.connect(peer);
			}else{
				peer.ID = 1;
				peer.networkSize = 1;
				peer.peers = new Peer[1];
				peer.peers[0] = peer;
			}
			
			MessageProxy messageProxy = peer.new MessageProxy();
			messageProxy.start();
			
			UI ui = peer.new UI();
			ui.start();
		} catch (RemoteException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		}
	}
	
	public int getID() throws RemoteException{
		return ID;
	}
	private void broadcastEntry(){
		Message msg = new EntryMessage(this, ID);
		msg.broadcast(peers);
	}

	
	// ----------------------------------------Message system-----------------------------------
 	public void message(Message msg) throws RemoteException{
		messages.add(msg);
	}
	
 	public void connect(Peer peer) throws RemoteException{
		System.out.println("A new peer is trying to connect");
		
			if (ID == networkSize){
				if (peers == null){
					System.out.println("ERROR: connect(), peers[] is not initiated");
					return;
				}
				
				
				// ----------------------------- TODO : peers should be expanded first if ID+1 = 2^k for any integer k. 
				peer.accept(ID+1, peers.clone(), this);
				
				
				if (ID == 1){ // If the network has just one peer
					prevPeer = peer;
					peers[0] = peer;
				}
				return;
			}else{
				if (peers == null){
					System.out.println("ERROR: connect(), ID, networkSize and peers[] does not correspond");
					return;
				}
				for (int i = 0; i < peers.length; i++){
					if (ID + ((i+1)^2) > networkSize){
						peers[i].connect(peer);
						return;
					}
				}
			}
	}
	
	public void accept(int ID, Peer[] peers, Peer prevPeer) throws RemoteException {
		this.ID = ID;
		this.networkSize = ID;
		this.peers = peers;
		this.prevPeer = prevPeer;
		System.out.println("I have been accepted!");
		broadcastEntry();
	}
	
	public Peer getPrevPeer() throws RemoteException{
		return prevPeer;
	}
	
	public static int log2(int n){
	    if(n <= 0) throw new IllegalArgumentException();
	    return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	
	// ------------------------------ Console User Interface---------------------------------
	
	public class UI extends Thread{
		public UI(){}
		public void run(){
			while(true){
				System.out.println("Options: exit, ID, size, peers, ping");
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			        String input = br.readLine();
					if (input.equals("exit")){
						System.exit(0);
					}
					if (input.equals("ID")){
						System.out.println(peer.ID);
					}
					if (input.equals("size")){
						System.out.println(peer.networkSize);
					}
					if (input.equals("peers")){
						System.out.println(peer.peers.length);
					}
					if (input.equals("ping")){
						Message msg = new StringMessage("Ping from : " + ID);
						msg.broadcast(peers);
					}
				} catch (IOException e) {
					System.out.println("ERROR: UI.run()");
					e.printStackTrace();
				}
			}
		}
	}
	
	public class MessageProxy extends Thread{
		public MessageProxy(){}
		
		public void run(){
			while(true){
				Message msg;
				try {
					msg = messages.take();
					msg.action(peer);
				} catch (InterruptedException e) {
					System.out.println("ERROR: MessageProxy()");
					e.printStackTrace();
				}
				
			}
		}
	}
}



//--------------------------------------------------------------------------------------------------

