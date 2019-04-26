package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class P2PLib implements Runnable{
	
	private User user;
	
	private Socket clientSocket;
	private ServerSocket server;
    //final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(2);

/*
	public P2PLib(User usr, Socket clientSocket2) {
		this.user= usr;
		this.clientSocket= clientSocket2;
		
	}*/
	
	public P2PLib(User usr, int Port) throws Exception {
		this.user= usr;
		try {
			server = new ServerSocket(Port);
			
			
		} catch (BindException e) {
			// TODO Auto-generated catch block
			System.out.println("port already in use, choose another");

			throw new Exception("port occupied");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public void run() {

        
        try {
            
        	while (true) {
    			clientSocket= server.accept();
    			new Thread(new ClientTask(clientSocket)).start();

	            
        	}

        } catch (IOException ex) {
            System.out.println("Unable to get streams from client");
        } catch(NullPointerException npe) {
        	
        	System.out.println("should choose another user");           

        }
        
        
	}
	
	private class ClientTask implements Runnable {
        private final Socket clientSocket;
		ObjectInputStream in = null;
        ObjectOutputStream out = null;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        @Override
        public void run() {
            System.out.println("Got a client !");
            
            try {
            	while(true) {
		            // Do whatever required to process the client's request
    	            out = new ObjectOutputStream(clientSocket.getOutputStream()); 
    	            in = new ObjectInputStream(clientSocket.getInputStream());
		           /* out = new ObjectOutputStream(client.getOutputStream()); 
		            in = new ObjectInputStream(client.getInputStream());*/
		            
			        Message cmd = (Message) in.readObject();
			       
						Message res = user.execute(cmd);
						out.writeObject(res);
            	}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    server.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
	

}
