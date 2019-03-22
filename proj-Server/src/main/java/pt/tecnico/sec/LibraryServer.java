package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
		 BufferedReader in = null;
	        PrintWriter out = null;
	        
	        try {
	            
	        	while (true) {
	        		out = new PrintWriter(clientSocket.getOutputStream(), true);
	    	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		           /* out = new ObjectOutputStream(client.getOutputStream()); 
		            in = new ObjectInputStream(client.getInputStream());*/
		            
			        String cmd = in.readLine();
			        try {
						String res = notary.execute(cmd);
						out.println(res);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
	        	}

	        } catch (IOException ex) {
	            System.out.println("Unable to get streams from client");
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
