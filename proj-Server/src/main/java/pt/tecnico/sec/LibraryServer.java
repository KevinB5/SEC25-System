package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

public final class LibraryServer  implements Runnable {

	public static final int PORT_NUMBER = 8081;
	private Notary notary;
	private Socket clientSocket;

    public LibraryServer(Socket client, Notary notary) {
    	this.notary = notary;
    	this.clientSocket = client;
       
    }

	@Override
	public void run() {
		 ObjectInputStream in = null;
	        ObjectOutputStream out = null;
	        
	        try {
	            out = new ObjectOutputStream(clientSocket.getOutputStream()); 
	            in = new ObjectInputStream(clientSocket.getInputStream());
	            
	        	while (true) {
	        		/*
	        		out = new PrintWriter(clientSocket.getOutputStream(), true);
	    	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));*/

		            
			        Message msg = (Message) in.readObject();
			        
			        String cmd = msg.getText();
			        String[] spl = cmd.split(" ");
			        
			        if (spl[0].equals("StoreKey")) {
			        	if(PKI.getInstance().getKey(msg.getID())== null) {
			        		out.writeObject( PKI.getInstance().setKey(msg.getID(), (PublicKey) msg.getObj()));
			        		
			        	}
			        }
			        else {
			        	
				        try {
							Message res = notary.execute(msg);
							out.writeObject(res);;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
	        	}

	        } catch (IOException | ClassNotFoundException ex) {
	            System.out.println("Unable to get streams from client");
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            try {
	                in.close();
	                out.close();
	                clientSocket.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }		
	}

   
}
