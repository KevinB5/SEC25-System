package pt.tecnico.sec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Aplicacao principal, lanca o notario e o Public Key Infraestructure
 *
 */
public class App 
{
	private static Notary notary;
	private static PKI pki;
	private static ServerSocket serverSocket;
	private static final int PORT = 8081;
    private static PrintWriter out;
    private static BufferedReader in;

	
    public static void main( String[] args ) throws GeneralSecurityException, IOException
    {
    	
    	
    	//notario liga o listener
    	notary= new Notary();//atribuir aqui a porta

    	//pki = new PKI();
    	
    	//notario aceita o cliente
    	//pki gera as chaves e pki atribui a chave ao user que ligou
    	//KeyPair keyPair = pki.createKeys(""); //usar userID
    	
    //Notary.setPrivateKey(keyPair.getPrivate());
    	//notario recebe pedidos do cliente

        // Convert port from String to int

        // Create server socket
        try {
			serverSocket = new ServerSocket(PORT);
	        System.out.println("Server accepting connections on port: "+ PORT);
	        
	        while (true) {

	        Socket clientSocket = serverSocket.accept();
	        
	        new Thread(new LibraryServer(clientSocket, notary)).start();
	        }
	        
	        
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
      
}
