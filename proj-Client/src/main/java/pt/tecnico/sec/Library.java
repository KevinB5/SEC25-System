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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	private Socket servConnect;
	private ServerSocket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private ObjectOutputStream outU;
    private ObjectInputStream inU;
    private String ip;
    private final String idUser;   
    private static final String SELL = "sell";
    private static final String STATE = "state";
    private static final String BUY = "buy";
    private static final String TRANSFER = "transfer";
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	//private HashMap <String, ObjectInputStream> readers = new HashMap<String, ObjectInputStream>();
	private static final String exceptMessage = "Must sign message first";

    private static int PORT;
    private User user;
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

public Message sendKey(PublicKey key) throws InvalidKeyException, Exception {
	   Message epa =send(new Message(this.idUser, "StoreKey",user.sign("StoreKey"),null, key, null));
	   return epa;
   }

public PublicKey getKey(String uid) throws InvalidKeyException, Exception {
	String msg = "Get "+uid;
	Message result = send(new Message(this.idUser, msg,user.sign(msg) , null, null, null ));
	return (PublicKey) result.getObj();
}


	public String intentionToSell(String userID, String goodID, String counter) throws InvalidKeyException, Exception {
		String msg =SELL +  " " +goodID + " "+ counter;
		
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		
		return result.getText();
	}

	
	public String getStateOfGood(String goodID, String challenge) throws InvalidKeyException, Exception {
		String msg= STATE + " " + goodID + " "+challenge;
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		String[] split = result.getText().split(" ");
		if(split[split.length-1].equals(challenge)) {
			//Get message from Notary excluding challenge and counter
			String text="";
			int i;
			for(i=0;i<split.length-2;i++) {
				text+=split[i]+" ";
			}
			System.out.println("STATE from notary: " +result.getText());
		}else
		System.out.println("STATE from notary: notary failed challenge");
		//returns counter
		return split[split.length-2];
	}

	
	public String transferGood(String userID, String buyer,String goodID, String counter, byte[] buyerSig) throws InvalidKeyException, Exception {
		String msg=TRANSFER +" "+ buyer+" "+ goodID +" "+ counter; 
		Message result=  send( new Message(idUser, msg, user.sign(msg),buyerSig, null, null));
	
		return result.getText();
	}

	
/****Connection to the server ****/////	
	
	public void connectServer(String Sip, int Sport) {
		try {

			this.servConnect = new Socket(Sip, Sport);
			System.out.println("connected to server at port: "+ Sport);
            out = new ObjectOutputStream(servConnect.getOutputStream()); 
            in = new ObjectInputStream(servConnect.getInputStream());
			System.out.println("all good");

		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	
	public void connectUser( String Uip,String userID, int Uport) {
		try {
			System.out.println("connecting to "+userID+"...");

			Socket clientSocket = new Socket(Uip, Uport);
			System.out.println("connected to server at port: "+ Uport);
			this.sockets.put(userID, clientSocket);
			
		}catch(IOException ie) {
			System.out.println(userID + " is not connected");
			//ie.printStackTrace();
		}
	} 
	
	public Message send(Message intent) throws Exception {
		if(intent.getSig().equals(null))
			throw new Exception(exceptMessage);
		Message res = null;
		try {
			out.writeObject(intent);
			res = (Message)in.readObject();
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
	
	 public Message sendMessage(String uID, Message msg) throws Exception {
		 ObjectOutputStream printer= null;
		 ObjectInputStream reader=null;
		 Message resp= null;
		 try {
			 System.out.println("Sending message to "+ uID);
		 	
			 Socket clientSocket = sockets.get(uID);
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
		 		if (temp.getClass().equals(String.class))
		 			System.out.println("STRING");
		 			//TODO: Falta fazer com que leia String
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
	    
	 
	 public void stopConnectServer() {
	        try {
				in.close();
		        out.close();
		        servConnect.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
	 
	 public void stopConnectUsers() {
	        try {
	        	for(Socket clientSocket : sockets.values()) {
				inU.close();
		        outU.close();
		        clientSocket.close();
	        	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }


	public Message buyGood(String userID, String goodID, String counter) throws Exception {//buyerID, goodID
		String msg = "intentionbuy " +goodID +" "+ counter;
		//manda 
		Message res= sendMessage(userID,new Message(idUser, msg, user.sign(msg),null, null, null));
		return res;
		//return sendMessage(userID,new Message(idUser, msg,user.sign(msg),null, null, null));
		//sendMessage(0,msg);
		
	}



	public String getStateOfGoodInvisible(String goodID, String challenge) throws InvalidKeyException, Exception {
		String msg= STATE + " " + goodID + " "+challenge;
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		String[] split = result.getText().split(" ");
		if(split[split.length-1].equals(challenge)) {
		return split[split.length-2];}
		else
			return "";
	}
/*
	
	public String sellGood(String userID, String buyerID, String goodID,byte[] buyerSig) throws InvalidKeyException, Exception {//buyerID, goodID
		System.out.println("Confirming transfer with notary");

		String res = this.transferGood(userID, buyerID, goodID, buyerSig);
		
		return res;
	}
*/
}
