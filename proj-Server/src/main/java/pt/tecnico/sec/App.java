package pt.tecnico.sec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Aplicacao principal, lanca o notario e o Public Key Infraestructure
 *
 */
public class App 
{
	private static Notary notary;
	private static ServerSocket serverSocket;
	private static final int PORT = 8081;

	
    public static void main( String[] args ) throws GeneralSecurityException, IOException
    {
    	notary= new Notary();//atribuir aqui a porta
        try {
			serverSocket = new ServerSocket(PORT);
	        System.out.println("Server accepting connections on port: "+ PORT);
	        while (true) {
	        	Socket clientSocket = serverSocket.accept();
	        	(new Thread(new LibraryServer2(clientSocket, notary))).start();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
  
    public final static class LibraryServer2  implements Runnable {

    	public static final int PORT_NUMBER = 8081;
    	private Notary notary;
    	private Socket clientSocket;

        public LibraryServer2(Socket client, Notary notary) {
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
    			        Message msg = (Message) in.readObject();
    			        String cmd = msg.getText();
    			        String[] spl = cmd.split(" ");
    				        try {
    							Message res = notary.execute(msg);
    							out.writeObject(res);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    			        }

    	        } catch (IOException | ClassNotFoundException ex) {
    	            System.out.println("Unable to get streams from client");
    	        } catch (Exception e) {
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
}
