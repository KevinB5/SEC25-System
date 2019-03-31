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
import java.util.Scanner;

public class Library {
	
	private Socket clientSocket;
	private ServerSocket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private PrintWriter outU;
    private BufferedReader inU;
    private String ip;
    private final String idUser;   
    private static final String SELL = "sell";
    private static final String STATE = "state";
    private static final String BUY = "buy";
    private static final String TRANSFER = "transfer";
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";
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
    

   public PublicKey getKey(String uID) {
	   Message msg = new Message(idUser, "getKey uID",null, null );
	   
	   Message ret = send(msg);
	   return (PublicKey) ret.getObj();
   }
   
   public Message sendKey(PublicKey key) {
	   Message epa =send(new Message(this.idUser, "StoreKey",null, key));
	   return epa;
   }


	public String intentionToSell(String userID, String goodID) throws InvalidKeyException, Exception {
		String res =SELL +  " " +goodID+ " ";
		String msg = res + new String(user.sign(res));
		
		Message result=  send( new Message(idUser, msg, null, null));
		
		return result.getText();
	}

	
	public String getStateOfGood(String goodID) {
		String msg= STATE + " " + goodID;
		Message result=  send( new Message(idUser, msg, null, null));
		return result.getText();
	}

	
	public String transferGood(String userID, String buyer,String goodID) {
		String msg=TRANSFER +" "+userID +" "+ buyer+" "+ goodID; 
		Message result=  send( new Message(idUser, msg, null, null));
		
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

	
	public void connectUser(String Uip, int Uport) {
		try {
			System.out.println("heres nothing");

			clientSocket = new Socket(Uip, Uport);
			System.out.println("connected to server at port: "+ Uport);
			outU = new PrintWriter(clientSocket.getOutputStream(), true);
			inU= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("all good");

			
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	} 
	
	public Message send(Message intent) {
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
	
	
	
	 public String sendMessage(int mode, String msg) throws Exception {
		 PrintWriter printer;
		 BufferedReader reader;
		 String resp= "";
		 try {
		 	if(mode == 0) {
		 		printer= outU;
		 		reader = inU;
		 	}
		 		outU.println(msg);
	        
	        
			
				resp = inU.readLine();
				//return execRequest(resp);
				//this.stopConnectServer();
				System.out.println(resp);
				return resp;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(NullPointerException ne) {
				throw new Exception("Must connect to user first");
			
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


	public String buyGood(String userID, String goodID) throws Exception {//buyerID, goodID
		String msg = "intentionbuy "+ userID +" " +goodID;
		//Message result = send(new Message(idUser, msg,null, null));
		//sendMessage(0,msg);
		
		return sendMessage(1, msg);
	}


	
	public String sellGood(String userID, String buyerID, String goodID) {//buyerID, goodID
		System.out.println("Confirming with notary");

		String res = this.transferGood(userID, buyerID, goodID);
		
		return res;
	}

}
