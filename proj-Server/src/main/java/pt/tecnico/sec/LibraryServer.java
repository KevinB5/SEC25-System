package pt.tecnico.sec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public final class LibraryServer extends Thread {

	public static final int PORT_NUMBER = 8081;


    private LibraryServer() {

        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Socket client = null;
        
        try {
        	ServerSocket socket = new ServerSocket(PORT_NUMBER);
            
        	while (true) {
	        	client = socket.accept();
	            
	            out = new ObjectOutputStream(client.getOutputStream()); 
	            in = new ObjectInputStream(client.getInputStream());
	            
	            Good good = (Good) in.readObject();
        	}

        } catch (IOException ex) {
            System.out.println("Unable to get streams from client");
        } catch (ClassNotFoundException e) {
        	System.out.println("Class not found exception");
        } finally {
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

   
}
