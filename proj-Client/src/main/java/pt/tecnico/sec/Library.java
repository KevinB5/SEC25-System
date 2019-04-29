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
	private ArrayList<Socket> servConnects = new ArrayList<Socket>();
	private ServerSocket serverSocket;

	private int n;
	private int f;
    private ObjectOutputStream[] out= new ObjectOutputStream[n];
    private ObjectInputStream[] in=new ObjectInputStream[n];
    
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
	private boolean[] acklist= new boolean[n];	
	private Pair[] statelist = new Pair[n];
	private Pair[] counterlist = new Pair[n];
	
   // private PKI pki = new PKI(PKI.KEYSIZE);
    
    /*
     * HashMap that returns the most recent buyer signature from buyerID with an IntentToBuy goodID 
     *  from the pair <BuyerID,GoodID>
     */
    //private HashMap<Pair<String,String>, byte[]> buyerSigs = new HashMap<Pair<String,String>,byte[]>();
    
    
    public Library(User user, String _ip, int _port) {
    	this.ip =_ip;
    	this.connectServer(_ip, _port);
    	this.idUser = user.getID();
    	this.PORT = user.gtPort();
    	this.user = user;
    	
    }
    
    class Pair {
    	  final String value;
    	  final int timestamp;
    	  Pair(String x, int y) {this.value=x;this.timestamp=y;}
    	}
    
    public void connectServer(String Sip, int Sport) {
    	for(int x=0;x<this.n;x++) {
    		try {
    			Thread.sleep(500);
    			Socket servConnect = new Socket(Sip, Sport+x);
    			System.out.println("connected to server at port: "+ Sport);
                out[x] = new ObjectOutputStream(servConnect.getOutputStream()); 
                in[x] = new ObjectInputStream(servConnect.getInputStream());
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
		for(int x=0;x<this.n;x++) {
		try {
			out[x].writeObject(intent);
			res = (Message)in[x].readObject();
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
		Message res = null;
		for(int x=0;x<this.n;x++) {
		try {
			out[x].writeObject(intent);
			res = (Message)in[x].readObject();
			String ts=res.getText().split(" ")[1];
			if(PKI.verifySignature(res.getText(),res.getSig(),res.getID())
					&& res.getText().equals("ACK") 
					&& ts.equals(wts)) {
				this.acklist[x]=true;
				if(acks()> (n+f)/2) {
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
	
	public String read(Message intent, int rid) throws Exception {
		clearReadLists();
		Message res = null;
		for(int x=0;x<this.n;x++) {
		try {
			out[x].writeObject(intent);
			res = (Message)in[x].readObject();
			String[] split =res.getText().split(" ");
			String r = split[3];
			if(PKI.verifySignature(res.getText(),res.getSig(),res.getID())
					&& Integer.parseInt(r)==rid) {
				int ts = Integer.parseInt(split[-1]);
				this.statelist[x]=new Pair(split[0], ts);
				this.counterlist[x]=new Pair(split[1], ts);
				if(this.reads() > (n+f)/2) {
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
	
	
	private String maximumValue(Pair[] array) {
		int max = 0;
		String maxval=null;
		for(int x=0 ; x < this.n ; x++) {
			if(array[x].timestamp>=max) {
				maxval=array[x].value;
			}
		}
		return maxval;		
	}
	
	private void clearAcklist() {
		for(int x=0 ; x < this.n ; x++) {
			this.acklist[x] = false;
		}
	}
	
	
	private void clearReadLists() {
		for(int x=0 ; x < this.n ; x++) {
			this.statelist[x] = null;
			this.counterlist[x]=null;
		}
	}
	
	private int reads() {
		int k=0;
		for(int x=0 ; x < this.n ; x++) {
			if(this.counterlist[x]!=null) {
			k++;	
			}
		}
		return k;
	}
	
	
	private int acks() {
		int k=0;
		for(int x=0 ; x < this.n ; x++) {
			if(this.acklist[x] = true) {
				k++;
			}
		}
		return k;
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
