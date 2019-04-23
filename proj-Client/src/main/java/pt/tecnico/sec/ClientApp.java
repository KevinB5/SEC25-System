package pt.tecnico.sec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;



public class ClientApp 
{
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	
	private static User user;
	private static User user2;
	//Streams for Users
	private ObjectOutputStream outU;
	private ObjectInputStream inU;
	//Streams for Notary
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Socket servConnect;

	
	private static final String IP = "127.0.0.1";
	private static final int PORT= 8081;
	private static final String ID ="user";
//	private static ServerSocket server;
//	private static Socket clientSocket;

	

    public static void main( String[] args ) throws BindException
    {

    	
        System.out.println("Select the user ID number");
        String nu;
        int nUsr=-1;//not a valid user number to begin with
        nu= System.console().readLine();
        try {
        	nUsr = Integer.parseInt(nu);
        }catch(NumberFormatException nef) {
        	System.out.println("Number must be an integer");
        }


        try {
	        user = new User(ID + nUsr, IP, PORT);//recebe o ip e a porta 
        }catch(IOException ioe) {
        	System.out.println(ioe.getMessage());
        }catch (Exception login) {
        	login.printStackTrace();
        }
	        	
        
            System.out.println( ID+nUsr +" initialized");
            /*
             * 
             * HERE WE CAN INCLUDE A PRINTOUT WITH A SHORT LISTING OF ALLOWED OPERATIONS TO THE NOTARY
             * Such as:
             * 
             * Greetings User1, here's a shortlist of commands you might find useful:
             * 
             * Intention to Sell: "notary <sellerID> <goodID,goodcounter>"
             * ... 
             *
             */
            
            
            
            int cPort = user.gtPort();
            new Thread(new P2PLib(user, cPort)).start();



			try {
	//			serverSocket = new ServerSocket(cPort);
				//server = new ServerSocket(cPort);

				
				 while(true) {
			        	
			            String op = System.console().readLine();
			            
						user.readOperation (op);	       			 
			         							
				 }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
        //cliente liga ao notario
       


        }






   
    
    
    
    
    
    

	
	
	public void disconnectServer() {
        try {
			in.close();
	        out.close();
	        servConnect.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
 
 public void disconnectUsers() {
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
	

 
 
    }
        
