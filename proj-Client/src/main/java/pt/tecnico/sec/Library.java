package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	private ServerSocket serverSocket;

	private int n;
	private int f=1;
    private HashMap<String,Socket> servers = new HashMap<String,Socket>();
    
    private ObjectOutputStream outU;
    private ObjectInputStream inU;
    private String ip;
    private final String idUser;   
	//private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	//private HashMap <String, ObjectInputStream> readers = new HashMap<String, ObjectInputStream>();
	private static int PORT;
    private User user;
    
	//Byzantine
	private HashMap<String,Boolean> acklist= new HashMap<String,Boolean>();	
	private HashMap<String,Pair> statelist = new HashMap<String,Pair>();
	private HashMap<String,Pair> counterlist = new HashMap<String,Pair>();
	
   // private PKI pki = new PKI(PKI.KEYSIZE);
    
    /*
     * HashMap that returns the most recent buyer signature from buyerID with an IntentToBuy goodID 
     *  from the pair <BuyerID,GoodID>
     */
    //private HashMap<Pair<String,String>, byte[]> buyerSigs = new HashMap<Pair<String,String>,byte[]>();
   
    
    public Library(User user, String _ip, HashMap<String, Integer> servPorts) {
    	this.ip =_ip;
    	this.connectServer(_ip, servPorts);
    	this.idUser = user.getID();
    	this.PORT = user.gtPort();
    	this.user = user;
    
    	
    }
    
    class Pair {
    	  final String value;
    	  final int timestamp;
    	  Pair(String x, int y) {this.value=x;this.timestamp=y;}
    	}
    
	public void connectServer(String ip, HashMap<String, Integer>servPorts) {
		Socket servConnect = null;
		for(int port : servPorts.values()) {
			try {
				
				Thread.sleep(500);
				servConnect = new Socket(ip, port);
				System.out.println("connected to server at port: "+ port);
				String sId="";
				for(String id : servPorts.keySet()) {
					if(servPorts.get(id).equals(port))
						sId = id;
					System.out.println(servPorts.get(id));
				}
					
	            servers.put(sId, servConnect); 
	            n++;
	//    			System.out.println("all good");
	            //servConnects.add(servConnect);
	          
			
			}catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
    	
	}
    
    public void connectUser( String Uip,String userID, int Uport) {
		try {
			System.out.println("connecting to "+userID+" in port " + Uport);

			Socket clientSocket = new Socket(Uip, Uport);
//			System.out.println("connected to server at port: "+ Uport);
			this.sockets.put(userID, clientSocket);
			
		}catch(ConnectException cnn) {
			System.out.println(userID + " is not connected");
			System.out.println(cnn.getMessage());
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
    
/*
   public PublicKey getKey(String uID) throws InvalidKeyException, Exception {
	   String ms = "getKey uID";
	   Message msg = new Message(idUser,ms ,user.sign(ms), null, null, null );
	   
	   Message ret = send(msg);
	   return (PublicKey) ret.getObj();
   }
  */
    /*
    public PublicKey getKey(String uID) throws InvalidKeyException, Exception {
    	return PKI.getKey(uID);
    }
    */
//    public void updateBuyerSigs(String buyerID, String goodID, byte[] buyersignature) {
//    	buyerSigs.put(new Pair<String,String>(buyerID,goodID),buyersignature );
//    }
    
    /*
     * Returns the correspondent most recent Buyer Signature
     *//*
   public byte[] getSig(String buyerID, String goodID) {
		return buyerSigs.get(new Pair<String,String>(buyerID,goodID));
	}

*/
/*
public Message sendKey(PublicKey key) throws InvalidKeyException, Exception {
	   Message epa =send(new Message(this.idUser, "StoreKey",user.sign("StoreKey"),null, key, null));
	   return epa;
   }

public PublicKey getKey(String uid) throws InvalidKeyException, Exception {
	String msg = "Get "+uid;
	Message result = send(new Message(this.idUser, msg,user.sign(msg) , null, null, null ));
	return (PublicKey) result.getObj();
}

*/
	

	
/****Connection to the server ****/////	
	public Message send(Message intent) throws Exception {
		if(intent.getSig().equals(null))
			throw new Exception("Must sign message first");
		Message res = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		for(String serv : servers.keySet()) {
			try {
				ouSt = new ObjectOutputStream(servers.get(serv).getOutputStream());
				inSt = new ObjectInputStream( servers.get(serv).getInputStream());
				ouSt.writeObject(intent);
				res = (Message)inSt.readObject();
				if(!PKI.verifySignature(res.getText(),res.getSig(),res.getID())) {
					throw new Exception("Invalid message signature");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;	
	}
    
	
	public String write(Message intent, int wts) throws Exception {
		clearAcklist();
		int acks=0;
		Message res = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		for(String serv : servers.keySet()) {
		try {
			ouSt = new ObjectOutputStream(servers.get(serv).getOutputStream());
			inSt = new ObjectInputStream( servers.get(serv).getInputStream());
			ouSt.writeObject(intent);
			res = (Message)inSt.readObject();
			int ts=Integer.parseInt(res.getText().split(" ")[1]);
			if(PKI.verifySignature(res.getText(),res.getSig(),res.getID())
					&& res.getText().split(" ")[0].equals("ACK") 
					&& ts==wts) {
				this.acklist.put(serv, true);
				acks++;
				if(acks> (n+f)/2) {
					return "OK";
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return "NOT OK";	
	}
	
	public String read(Message intent, int rid, String challenge) throws Exception {
		clearReadLists();
		int reads=0;
		Message res = null;
		ObjectOutputStream ouSt = null;
		ObjectInputStream inSt= null;
		System.out.println("sending: "+intent.getText());
		//System.out.println(out.keySet());
		for(String serv : servers.keySet()) {
			try {
								ouSt = new ObjectOutputStream(servers.get(serv).getOutputStream());
				inSt = new ObjectInputStream( servers.get(serv).getInputStream());
				ouSt.writeObject(intent);
				res = (Message)inSt.readObject();
				String[] split =res.getText().split(" ");
				String r = split[5];
				System.out.println("message ID, serv: "+res.getID()+", " + serv);
	//			System.out.println("correct message from " +res.getID()+":");
	//			System.out.println((PKI.verifySignature(res.getText(),res.getSig(),serv)
	//					&& Integer.parseInt(r)==rid
	//					&& split[3].equals(challenge)));
	//			System.out.println("correct signature: "+PKI.verifySignature(res.getText(),res.getSig(),serv));
	//			System.out.println("correct rid: "+ (Integer.parseInt(r)==rid));
	//			System.out.println("correct challenge: "+split[3].equals(challenge));
				if(PKI.verifySignature(res.getText(),res.getSig(),res.getID())
						&& Integer.parseInt(r)==rid
						&& split[3].equals(challenge)) {
	//				System.out.println("recording message from "+res.getID());
					int ts = Integer.parseInt(split[4]);
					statelist.put(serv,new Pair(split[1], ts));
					this.counterlist.put(serv,new Pair(split[2], ts));
					reads++;
					if(reads > (n+f)/2) {
						System.out.println("done reading");
						return maximumValue(statelist)+" "+maximumValue(counterlist);
					}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return "NOT OK";	
	}
	
	
	private String maximumValue(HashMap<String, Pair> statelist2) {
		int max = 0;
		String maxval=null;
		for(String serv : statelist2.keySet()) {
			if(statelist2.get(serv).timestamp>=max) {
				maxval=statelist2.get(serv).value;
			}
		}
		return maxval;		
	}
	
	private void clearAcklist() {
		for(String sv : acklist.keySet()) {
			this.acklist.replace(sv, false);
		}
	}
	
	
	private void clearReadLists() {
		
		statelist = new HashMap<String,Pair>();
		counterlist = new HashMap<String,Pair>();
	}
	

	public Message sendMessage(String uID, Message msg) throws Exception {
		 ObjectOutputStream printer= null;
		 ObjectInputStream reader=null;
		 Message resp= null;
		 try {
//			 System.out.println("Sending message to "+ uID);
		 	
			 Socket clientSocket = sockets.get(uID);
			 if(clientSocket == null)
				 throw new Exception("Must connect to user "+ uID+" first");
				outU = new ObjectOutputStream(clientSocket.getOutputStream());
				inU= new ObjectInputStream(clientSocket.getInputStream());
		 		
		 		outU.writeObject(msg);
		 		//outU.reset();
		 		//
		 		
				//
		 		//resp = (Message) inU.readObject();
				//return execRequest(resp);
				//this.stopConnectServer();
		 		Message temp = (Message) inU.readObject();
		 		if (temp.getClass().equals(String.class)) {
		 			System.out.println(temp);
		 			return null;
		 		}
		 		else {
		 			resp = (Message) temp;
		 		}
				//System.out.println("inU.readObject() " + inU.readObject());
				return resp;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(NullPointerException ne) {
				System.out.println(ne.getMessage());
			
			
			}catch(Exception e) {
				e.printStackTrace();
			}
			//resp = pki.encrypt(,resp); falta buscar a chave privada do user
	        return resp;

	    }
	
	
}

