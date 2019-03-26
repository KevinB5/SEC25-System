package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Library {
	
	private Socket clientSocket;
	private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
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
   // private PKI pki = new PKI(PKI.KEYSIZE);

    


    
    public Library(String id, String _ip, int _port, int PORT) {
    	this.ip =_ip;
    	this.connectServer(_ip, _port);
    	this.idUser = id;
    	this.PORT = PORT;
    	
    }
    

 


	public String intentionToSell(String userID, String goodID) {
		String msg =SELL + " " + userID + " " +goodID;
		
		return sendMessage(0, msg);
	}

	
	public String getStateOfGood(String goodID) {
		String msg= STATE + " " + goodID;
		
		return sendMessage(0, msg);
	}

	
	public String transferGood(String userID, String buyer,String goodID) {
		String msg=TRANSFER +" "+userID +" "+ buyer+" "+ goodID; 
		return sendMessage(0, msg);
	}

	
/****Connection to the server ****/////	
	
	public void connectServer(String Sip, int Sport) {
		try {

			clientSocket = new Socket(Sip, Sport);
			System.out.println("connected to server at port: "+ Sport);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
	
	 public String sendMessage(int mode, String msg) {
		 PrintWriter printer;
		 BufferedReader reader;
		 	if(mode == 0) {
		 		printer= out;
		 		reader= in;
		 	}
		 	
		 	else {
		 		printer= outU;
		 		reader = inU;
		 	}
		 		printer.println(msg);
	        String resp= "";
	        
			try {
				resp = reader.readLine();
				//return execRequest(resp);
				//this.stopConnectServer();
				System.out.println(resp);
				return resp;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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


	public String buyGood(String userID, String goodID) {//buyerID, goodID
		String msg = "intentionbuy "+ userID +" " +goodID;
		return sendMessage(1,msg );
	}


	
	public String sellGood(String userID, String buyerID, String goodID) {//buyerID, goodID
		System.out.println("Confirming with notary");

		String res = this.transferGood(userID, buyerID, goodID);
		
		return res;
	}

}
