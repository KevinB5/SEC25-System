package pt.tecnico.sec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

	
    public static void main( String[] args )
    {
    	notary= new Notary();//atribuir aqui a porta
    	//notario liga o listener
    	pki= new PKI();
    	//notario aceita o cliente
    	//pki gera as chaves
    	//pki atribui a chave ao user que ligou
    	//notario recebe pedidos do cliente
    	System.out.println("Fg mano");
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerSocket.class.getName());
            return;
        }

        // Convert port from String to int

        // Create server socket
        try {
			serverSocket = new ServerSocket(PORT);
	        System.out.println("Server accepting connections on port: "+ PORT);

	        Socket clientSocket = serverSocket.accept();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

    }
      
}
