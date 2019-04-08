package pt.tecnico.sec;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class ClientApp 
{
	private static User user;
	private static User user2;

	
	private static final String IP = "127.0.0.1";
	private static final int PORT= 8081;
	private static final String ID ="user";
	private static ServerSocket server;
	private static Socket clientSocket;

	

    public static void main( String[] args ) throws BindException
    {

    	
        System.out.println("Select the user ID number");
        String nu;
        nu= System.console().readLine();
        int nUsr = Integer.parseInt(nu);


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

        
    }
        
