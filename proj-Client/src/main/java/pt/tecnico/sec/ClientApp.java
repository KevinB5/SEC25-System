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
	private static HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	
	private static User user;
	private static User user2;
	//Streams for Users
	private static ObjectOutputStream outU;
	private static ObjectInputStream inU;
	//Streams for Notary
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static Socket servConnect;

	
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
        	
	        user = new User(ID + nUsr, IP, PORT);//recebe o ip e a porta 

        }catch(NumberFormatException nef) {
        	System.out.println("Number must be an integer");
        }catch(Exception ioe) {
        	System.out.println(ioe.getMessage());
        }
        
        try {
        	new Thread(new P2PLib(user, user.gtPort())).start();
        }catch(Exception e) {
        	//se o utilizador pedido ja existir
        	System.out.println("user already exists choose another");
        	
        	//pede outro inteiro
        	nUsr =Integer.parseInt(System.console().readLine());
	        try {
	        	//cria novo user
				user = new User(ID + nUsr, IP, PORT);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}//recebe o ip e a porta 

        	try {
        		//tenta fazer a conexao again
				new Thread(new P2PLib(user, user.gtPort())).start();
			} catch (NumberFormatException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        			
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
			}finally {
				disconnectServer();
				
				
			}

		
        //cliente liga ao notario
       


        }






   
    
    
    
    
    
    

	
	
	public static void disconnectServer() {
        try {
			in.close();
	        out.close();
	        servConnect.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
 
 public static void disconnectUsers() {
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
        
