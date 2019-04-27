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
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
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
	private int wts=0;
	private int f;
	private int n;
	
	
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
    
    public void connectServer(String Sip, int Sport) {
		try {

			Socket servConnect = new Socket(Sip, Sport);
			System.out.println("connected to server at port: "+ Sport);
            out = new ObjectOutputStream(servConnect.getOutputStream()); 
            in = new ObjectInputStream(servConnect.getInputStream());
//			System.out.println("all good");
            servConnects.add(servConnect);

            if(this.n==0) {
	            Message fMessage=null;
	            try {
	            	fMessage = (Message)in.readObject();
	    			this.f = Integer.parseInt(fMessage.getText());
	    			this.n= 3*f+1;
	    			System.out.println("THIS IS N: "+ n);
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (ClassNotFoundException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	            
	            for(int x = 1;x<this.n;x++) {
	            	connectServer(Sip,Sport+x);
	            }
            }

		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException ex) {
			ex.printStackTrace();
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
		try {
			out.writeObject(intent);
			res = (Message)in.readObject();
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
		return res;	
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
	
	
	
	/*
	
	public Message sendMessage(String uID, Message msg) throws Exception {
		Message res = null;
		try {
			outU.writeObject(msg);
			res = (Message)inU.readObject();
			System.out.println(res.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
		
	}
	*/
	
	    
	 
/*
	
	public String sellGood(String userID, String buyerID, String goodID,byte[] buyerSig) throws InvalidKeyException, Exception {//buyerID, goodID
		System.out.println("Confirming transfer with notary");

		String res = this.transferGood(userID, buyerID, goodID, buyerSig);
		
		return res;
	}
*/
}
