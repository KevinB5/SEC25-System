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
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	
	private Socket clientSocket;
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
	private HashMap <String, ObjectOutputStream> writters = new HashMap<String, ObjectOutputStream>();
	private HashMap <String, ObjectInputStream> readers = new HashMap<String, ObjectInputStream>();
	private static final String exceptMessage = "Must sign message first";

    private static int PORT;
    private User user;
   // private PKI pki = new PKI(PKI.KEYSIZE);

    


    
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
   public Message sendKey(PublicKey key) throws InvalidKeyException, Exception {
	   Message epa =send(new Message(this.idUser, "StoreKey",user.sign("StoreKey"),null, key, null));
	   return epa;
   }


	public String intentionToSell(String userID, String goodID) throws InvalidKeyException, Exception {
		String msg =SELL +  " " +goodID;
		
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		
		return result.getText();
	}

	
	public String getStateOfGood(String goodID, String challenge) throws InvalidKeyException, Exception {
		String msg= STATE + " " + goodID + " "+challenge;
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		return result.getText();
	}

	
	public String transferGood(String userID, String buyer,String goodID) throws InvalidKeyException, Exception {
		String msg=TRANSFER +" "+ buyer+" "+ goodID; 
		Message result=  send( new Message(idUser, msg, user.sign(msg),null, null, null));
		
		return result.getText();
	}

	
/****Connection to the server ****/////	
	
	public void connectServer(String Sip, int Sport) {
		try {

			clientSocket = new Socket(Sip, Sport);
			System.out.println("connected to server at port: "+ Sport);
            out = new ObjectOutputStream(clientSocket.getOutputStream()); 
            in = new ObjectInputStream(clientSocket.getInputStream());
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
			System.out.println("connecting to "+userID);

			clientSocket = new Socket(Uip, Uport);
			System.out.println("connected to server at port: "+ Uport);
			outU = new ObjectOutputStream(clientSocket.getOutputStream());
			inU= new ObjectInputStream(clientSocket.getInputStream());
			writters.put(userID, outU);
			readers.put(userID, inU);
			
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	} 
	
	public Message send(Message intent) throws Exception {
		if(intent.getSig().equals(null))
			throw new Exception(exceptMessage);
		Message res = null;
		try {
			out.writeObject(intent);
			res = (Message)in.readObject();
			
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
		 ObjectOutputStream printer;
		 ObjectInputStream reader;
		 Message resp= null;
		 try {
		 	
		 		printer= this.writters.get(uID);
		 		reader = this.readers.get(uID);
		
		 		outU.writeObject(msg);
	        
	        
			
				resp = (Message) inU.readObject();
				//return execRequest(resp);
				//this.stopConnectServer();
				System.out.println(resp);
				return resp;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(NullPointerException ne) {
				System.out.println("Must connect to user first");
			
			}catch(Exception e) {
				System.out.println("Operation not valid");
			}
			//resp = pki.encrypt(,resp); falta buscar a chave privada do user
	        return resp;

	    }
	    
	 
	 public void stopConnectServer() {
	        try {
				in.close();
		        out.close();
		        clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
	 
	 public void stopConnectUser() {
	        try {
				inU.close();
		        outU.close();
		        clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }


	public Message buyGood(String userID, String goodID, String counter) throws Exception {//buyerID, goodID
		String msg = "intentionbuy " +goodID +" "+ counter;
		//manda 
		Message result = new Message(idUser, msg,user.sign(msg),null, null, null);
		//sendMessage(0,msg);
		
		return sendMessage(userID, result);
		
	}


	
	public String sellGood(String userID, String buyerID, String goodID) throws InvalidKeyException, Exception {//buyerID, goodID
		System.out.println("Confirming with notary");

		String res = this.transferGood(userID, buyerID, goodID);
		
		return res;
	}

}
