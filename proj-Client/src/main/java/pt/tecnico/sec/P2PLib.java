package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
	public P2PLib(User usr, int Port) {
		this.user= usr;
		try {
			server = new ServerSocket(Port);
			
			
		} catch (BindException e) {
			// TODO Auto-generated catch block
			System.out.println("port already in use");
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
        } 
        
        
	}
	
	private class ClientTask implements Runnable {
        private final Socket clientSocket;
		BufferedReader in = null;
        PrintWriter out = null;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        @Override
        public void run() {
            System.out.println("Got a client !");
            
            try {
            	while(true) {
		            // Do whatever required to process the client's request
		            out = new PrintWriter(clientSocket.getOutputStream(), true);
			        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		           /* out = new ObjectOutputStream(client.getOutputStream()); 
		            in = new ObjectInputStream(client.getInputStream());*/
		            
			        String cmd = in.readLine();
			       
						String res = user.execute(cmd);
						out.println(res);
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
