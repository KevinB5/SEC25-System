package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Library implements ILibrary {
	
	private Socket clientSocket;
	private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String ip;
    private int port;
    private static final String SELL = "intentionSell";
    private static final String STATE = "goodState";
    private static final String BUY = "buyGood";
    private static final String TRANSFER = "transferGood";
    

    
    public Library(String _ip, int _port) {
    	this.ip =_ip;
    	this.port = _port;
    	this.connectServer(_ip, _port);
    }
 

	@Override
	public String intentionToSell(String userID, String goodID) {
		String msg =SELL + " " + userID + " " +goodID;
		
		return sendMessage(msg);
	}

	@Override
	public String getStateOfGood(String goodID) {
		String msg= STATE + " " + goodID;
		
		return sendMessage(msg);
	}

	@Override
	public String transferGood(String goodID) {
		String msg=TRANSFER +" "+ goodID; 
		return sendMessage(msg);
	}
	
	private String execRequest(String request) throws Exception{
		String [] res = request.split(" ");
		if(!res[0].equals(STATE) | res[0].equals(BUY)|res[0].equals(SELL)| res[0].equals(TRANSFER))
			throw new Exception("not valid operation");

		if(res[0].equals(STATE)) {
			String r= "";
			r =res[1]+" "+ res[2];
			return r;
		}
		
		else
			return res [1];
	}
	
	
/****Connection to the server ****/////	
	
	public void connectServer(String Sip, int Sport) {
		try {
			clientSocket = new Socket(Sip, Sport);
			System.out.println("connected to server");
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("all good");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void connectUser(String ip) {
		try {
			serverSocket = new ServerSocket(port);
	        Socket clientSocket = serverSocket.accept();

			
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
	
	 public String sendMessage(String msg) {
	        out.println(msg);
	        String resp= "";
	        
			try {
				resp = in.readLine();
				return execRequest(resp);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e) {
				System.out.println("Operation not valid");
			}
	        return resp;

	    }
	 
	 public void stopConnectServer() {
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
