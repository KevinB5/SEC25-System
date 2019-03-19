package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Library implements ILibrary {
	
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int ip;
    private int port;
    
    public Library(int _ip, int _port) {
    	this.ip =_ip;
    	this.port = _port;
    }
 

	@Override
	public boolean intentionToSell(String userID, String goodID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getStateOfGood(String goodID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transferGood(String goodID) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
/****Connection to the server ****/////	
	
	public void startConnection(String ip, int port) {
		try {
			clientSocket = new Socket(ip, port);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	 public String sendMessage(String msg) {
	        out.println(msg);
	        String resp= "";
			try {
				resp = in.readLine();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return resp;

	    }
	 
	 public void stopConnection() {
	        try {
				in.close();
		        out.close();
		        clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }

}
