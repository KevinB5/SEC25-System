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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

public class Library {
	private ArrayList<Socket> servConnects = new ArrayList<Socket>();
	private ServerSocket serverSocket;

	private int n;
	private int f=1;
    private HashMap<String,ObjectOutputStream> out = new HashMap<String,ObjectOutputStream>();
    private HashMap<String,ObjectInputStream> in = new HashMap<String,ObjectInputStream>();
    
    private ObjectOutputStream outU;
    private ObjectInputStream inU;
    private String ip;
    private final String idUser;   
	//private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	//private HashMap <String, ObjectInputStream> readers = new HashMap<String, ObjectInputStream>();
	private static int PORT;
    private User user;
    
    private final String hashLimit = "0000";
	//Byzantine
	private HashMap<String,Boolean> acklist= new HashMap<String,Boolean>();
	private HashMap<String,Recorded> readlist = new HashMap<String,Recorded>();
//	private HashMap<String,Pair> statelist = new HashMap<String,Pair>();
//	private HashMap<String,Pair> counterlist = new HashMap<String,Pair>();
//	private HashMap<String,byte[]> signaturelist = new HashMap<>
	
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
    
    class Recorded {
    	  String state;
    	  String counter;
    	  byte[] signature;
    	  int timestamp;
    	  Recorded(String x,String y,byte[] s, int t) {this.state=x;this.counter=y;this.signature=s;this.timestamp=t;}
    	}
    
	public void connectServer(String ip, HashMap<String, Integer>servPorts) {
		for(int port : servPorts.values()) {
			try {
				
				Thread.sleep(500);
				Socket servConnect = new Socket(ip, port);
				System.out.println("connected to server at port: "+ port);
				String sId="";
				for(String id : servPorts.keySet()) {
					if(servPorts.get(id).equals(port))
						sId = id;
				}
					
	            out.put(sId, new ObjectOutputStream(servConnect.getOutputStream())); 
	            in.put(sId, new ObjectInputStream(servConnect.getInputStream()));
	            n++;
	//    			System.out.println("all good");
	            servConnects.add(servConnect);
	          
			
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
		for(String serv : out.keySet()) {
			try {
				ouSt = out.get(serv);
				inSt = in.get(serv);
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
		for(String serv : out.keySet()) {
		try {
			ouSt = out.get(serv);
			inSt = in.get(serv);
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
		clearReadList();
		int reads=0;
		Message res,writeback = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		System.out.println("sending: "+intent.getText());
		System.out.println(out.keySet());
		for(String serv : out.keySet()) {
		try {
			ouSt = out.get(serv);
			inSt = in.get(serv);
			ouSt.writeObject(intent);
			res = (Message)inSt.readObject();
			String[] split =res.getText().split(" ");
			System.out.println("res: "+res.getText());
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
						System.out.println("recording message from "+res.getID());
						System.out.println(res.getText());
						int ts = Integer.parseInt(split[4]);
			//				statelist.put(serv,new Pair(split[1], ts));
			//				this.counterlist.put(serv,new Pair(split[2], ts));
						//Recorded(state,counter,sig,timestamp)
						readlist.put(serv,new Recorded(split[1],split[2],res.getWriteSignature(),ts));
						reads++;
				if(reads > (n+f)/2) {
//					TODO: WriteBack here:
					writeback = new Message(split[0],"sell "+split[2], null, null, null, null);
					return split[0]+" "+maximumValue(readlist);
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
	
	
	private String maximumValue(HashMap<String, Recorded> statelist2) {
		int max = 0;
		String maxstate=null;
		String maxcounter=null;
		byte[] maxsig;
		//TODO: maxsig not being returned!
		for(String serv : statelist2.keySet()) {
			if(statelist2.get(serv).timestamp>=max) {
				maxstate=statelist2.get(serv).state;
				maxcounter=statelist2.get(serv).counter;
				maxsig=statelist2.get(serv).signature;
			}
		}
		return maxstate +" " +maxcounter;		
	}
	
	private void clearAcklist() {
		for(String sv : acklist.keySet()) {
			this.acklist.replace(sv, false);
		}
	}
	
	
	private void clearReadList() {
		readlist = new HashMap<String,Recorded>();
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
	
	public String hash(String content) {
		MessageDigest digest;
		byte[] hash = null;
		String hashString= null;
		int i=0;
		do {
			hashString = content+i;
			try {
				digest = MessageDigest.getInstance("SHA-256");
				hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));
				hashString = DatatypeConverter.printHexBinary(hash);
				hashString = hashString.substring(0,4);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			i++;
		}
		while(!hashString.equals(hashLimit)); 
		return ""+i;
	}
}
